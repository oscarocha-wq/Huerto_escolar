package cl.duoc.huerto.data.repository

import cl.duoc.huerto.data.local.dao.SectorDao
import cl.duoc.huerto.domain.model.DependenciasSector
import cl.duoc.huerto.domain.model.Sector
import cl.duoc.huerto.domain.repository.SectorRepository
import kotlinx.coroutines.flow.Flow

class SectorRepositoryImpl(private val dao: SectorDao) : SectorRepository {

    override fun sectores(): Flow<List<Sector>> = dao.todos()

    override fun observar(id: Int): Flow<Sector?> = dao.observar(id)

    override suspend fun porId(id: Int): Sector? = dao.porId(id)

    override suspend fun codigoDuplicado(codigo: String, idExcluido: Int): Boolean =
        dao.contarPorCodigo(codigo, idExcluido) > 0

    override suspend fun guardar(sector: Sector) {
        if (sector.id == 0) dao.insertar(sector) else dao.actualizar(sector)
    }

    override suspend fun dependencias(id: Int): DependenciasSector = DependenciasSector(
        cultivos = dao.contarCultivos(id),
        tareas = dao.contarTareas(id),
        problemas = dao.contarProblemas(id)
    )

    override suspend fun eliminar(sector: Sector) = dao.eliminar(sector)
}
