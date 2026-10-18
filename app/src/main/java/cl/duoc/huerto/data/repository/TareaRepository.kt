package cl.duoc.huerto.data.repository

import cl.duoc.huerto.data.dao.TareaDao
import cl.duoc.huerto.data.entities.Tarea
import kotlinx.coroutines.flow.Flow

class TareaRepository(private val dao: TareaDao) {
    fun todas(): Flow<List<Tarea>> = dao.todas()
    fun paraEstudiante(usuarioId: String, grupo: String?): Flow<List<Tarea>> =
        dao.paraEstudiante(usuarioId, grupo)
    fun observar(id: Int): Flow<Tarea?> = dao.observar(id)
    suspend fun porId(id: Int): Tarea? = dao.porId(id)
    suspend fun crear(tarea: Tarea) { dao.insertar(tarea) }
    suspend fun actualizar(tarea: Tarea) = dao.actualizar(tarea)
}