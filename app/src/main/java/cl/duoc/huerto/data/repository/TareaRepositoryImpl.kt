package cl.duoc.huerto.data.repository

import cl.duoc.huerto.data.local.dao.TareaDao
import cl.duoc.huerto.domain.model.Tarea
import cl.duoc.huerto.domain.repository.TareaRepository
import kotlinx.coroutines.flow.Flow

class TareaRepositoryImpl(private val dao: TareaDao) : TareaRepository {
    override fun todas(): Flow<List<Tarea>> = dao.todas()
    override fun paraEstudiante(usuarioId: String, grupo: String?): Flow<List<Tarea>> =
        dao.paraEstudiante(usuarioId, grupo)
    override fun observar(id: Int): Flow<Tarea?> = dao.observar(id)
    override suspend fun porId(id: Int): Tarea? = dao.porId(id)
    override suspend fun crear(tarea: Tarea) { dao.insertar(tarea) }
    override suspend fun actualizar(tarea: Tarea) = dao.actualizar(tarea)
    override suspend fun eliminar(tarea: Tarea) = dao.eliminar(tarea)
}
