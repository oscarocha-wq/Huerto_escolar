package cl.duoc.huerto.data.repository

import cl.duoc.huerto.data.local.dao.CultivoDao
import cl.duoc.huerto.data.remote.HuertoApi
import cl.duoc.huerto.data.remote.dto.toDto
import cl.duoc.huerto.data.remote.intentarRemoto
import cl.duoc.huerto.domain.model.Cultivo
import cl.duoc.huerto.domain.repository.CultivoRepository
import kotlinx.coroutines.flow.Flow

/** Cultivos: guarda en Room y, si hay api, también en el microservicio (POST / PUT / DELETE). */
class CultivoRepositoryImpl(
    private val dao: CultivoDao,
    private val api: HuertoApi? = null
) : CultivoRepository {

    override fun cultivos(): Flow<List<Cultivo>> = dao.todos()

    override fun porSector(sectorId: Int): Flow<List<Cultivo>> = dao.porSector(sectorId)

    override suspend fun porId(id: Int): Cultivo? = dao.porId(id)

    override suspend fun guardar(cultivo: Cultivo) {
        if (cultivo.id == 0) {
            // El servidor no guarda la foto: se conserva la ruta local
            val delServidor = api?.let { intentarRemoto { it.crearCultivo(cultivo.toDto(conId = false)) } }
                ?.toEntity()?.copy(fotoUri = cultivo.fotoUri)
            val usarServidor = delServidor != null && dao.porId(delServidor.id) == null
            dao.insertar(if (usarServidor) delServidor!! else cultivo)
        } else {
            dao.actualizar(cultivo)
            api?.let { intentarRemoto { it.actualizarCultivo(cultivo.id.toLong(), cultivo.toDto()) } }
        }
    }

    override suspend fun contarTareas(id: Int): Int = dao.contarTareas(id)

    override suspend fun eliminar(cultivo: Cultivo) {
        dao.eliminar(cultivo)
        api?.let { intentarRemoto { it.eliminarCultivo(cultivo.id.toLong()) } }
    }
}
