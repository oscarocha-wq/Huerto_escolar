package cl.duoc.huerto.data.repository

import cl.duoc.huerto.data.local.dao.SectorDao
import cl.duoc.huerto.data.remote.HuertoApi
import cl.duoc.huerto.data.remote.dto.toDto
import cl.duoc.huerto.data.remote.intentarRemoto
import cl.duoc.huerto.domain.model.DependenciasSector
import cl.duoc.huerto.domain.model.Sector
import cl.duoc.huerto.domain.repository.SectorRepository
import kotlinx.coroutines.flow.Flow

/**
 * Sectores: Room es la fuente que leen las pantallas.
 * Si hay api, cada cambio también se envía al microservicio (POST / PUT / DELETE).
 * Si el servidor no responde, el cambio queda igual guardado en el teléfono.
 */
class SectorRepositoryImpl(
    private val dao: SectorDao,
    private val api: HuertoApi? = null
) : SectorRepository {

    override fun sectores(): Flow<List<Sector>> = dao.todos()

    override fun observar(id: Int): Flow<Sector?> = dao.observar(id)

    override suspend fun porId(id: Int): Sector? = dao.porId(id)

    override suspend fun codigoDuplicado(codigo: String, idExcluido: Int): Boolean =
        dao.contarPorCodigo(codigo, idExcluido) > 0

    override suspend fun guardar(sector: Sector) {
        if (sector.id == 0) {
            // Crear: primero en el servidor para usar el mismo id en los dos lados
            val delServidor = api?.let { intentarRemoto { it.crearSector(sector.toDto(conId = false)) } }?.toEntity()
            val usarServidor = delServidor != null && dao.porId(delServidor.id) == null
            dao.insertar(if (usarServidor) delServidor!! else sector)
        } else {
            dao.actualizar(sector)
            api?.let { intentarRemoto { it.actualizarSector(sector.id.toLong(), sector.toDto()) } }
        }
    }

    override suspend fun dependencias(id: Int): DependenciasSector = DependenciasSector(
        cultivos = dao.contarCultivos(id),
        tareas = dao.contarTareas(id),
        problemas = dao.contarProblemas(id)
    )

    override suspend fun eliminar(sector: Sector) {
        dao.eliminar(sector)
        api?.let { intentarRemoto { it.eliminarSector(sector.id.toLong()) } }
    }
}
