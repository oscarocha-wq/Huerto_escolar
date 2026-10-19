package cl.duoc.huerto.data.repository

import cl.duoc.huerto.data.dao.CultivoDao
import cl.duoc.huerto.data.entities.Cultivo
import kotlinx.coroutines.flow.Flow

class CultivoRepository(private val dao: CultivoDao) {

    fun cultivos(): Flow<List<Cultivo>> = dao.todos()

    fun porSector(sectorId: Int): Flow<List<Cultivo>> = dao.porSector(sectorId)

    suspend fun porId(id: Int): Cultivo? = dao.porId(id)

    suspend fun guardar(cultivo: Cultivo) {
        if (cultivo.id == 0) dao.insertar(cultivo) else dao.actualizar(cultivo)
    }

    suspend fun contarTareas(id: Int): Int = dao.contarTareas(id)

    suspend fun eliminar(cultivo: Cultivo) = dao.eliminar(cultivo)
}
