package cl.duoc.huerto.domain.repository

import cl.duoc.huerto.domain.model.Tarea
import kotlinx.coroutines.flow.Flow

/** Contrato de acceso a tareas. La implementación vive en la capa data. */
interface TareaRepository {
    fun todas(): Flow<List<Tarea>>
    fun paraEstudiante(usuarioId: String, grupo: String?): Flow<List<Tarea>>
    fun observar(id: Int): Flow<Tarea?>
    suspend fun porId(id: Int): Tarea?
    suspend fun crear(tarea: Tarea)
    suspend fun actualizar(tarea: Tarea)
    suspend fun eliminar(tarea: Tarea)
}
