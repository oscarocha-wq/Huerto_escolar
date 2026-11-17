package cl.duoc.huerto.data.repository

import cl.duoc.huerto.data.local.dao.CultivoDao
import cl.duoc.huerto.domain.model.Cultivo
import cl.duoc.huerto.domain.repository.CultivoRepository
import kotlinx.coroutines.flow.Flow

class CultivoRepositoryImpl(private val dao: CultivoDao) : CultivoRepository {

    override fun cultivos(): Flow<List<Cultivo>> = dao.todos()

    override fun porSector(sectorId: Int): Flow<List<Cultivo>> = dao.porSector(sectorId)

    override suspend fun porId(id: Int): Cultivo? = dao.porId(id)

    override suspend fun guardar(cultivo: Cultivo) {
        if (cultivo.id == 0) dao.insertar(cultivo) else dao.actualizar(cultivo)
    }

    override suspend fun contarTareas(id: Int): Int = dao.contarTareas(id)

    override suspend fun eliminar(cultivo: Cultivo) = dao.eliminar(cultivo)
}
