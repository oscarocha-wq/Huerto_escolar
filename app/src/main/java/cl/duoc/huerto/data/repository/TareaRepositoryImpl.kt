package cl.duoc.huerto.data.repository

import cl.duoc.huerto.data.local.dao.TareaDao
import cl.duoc.huerto.data.remote.HuertoApi
import cl.duoc.huerto.data.remote.dto.EstadoTareaDto
import cl.duoc.huerto.data.remote.dto.toDto
import cl.duoc.huerto.data.remote.intentarRemoto
import cl.duoc.huerto.domain.model.Tarea
import cl.duoc.huerto.domain.repository.TareaRepository
import kotlinx.coroutines.flow.Flow

/** Tareas: guarda en Room y, si hay api, también en el microservicio. */
class TareaRepositoryImpl(
    private val dao: TareaDao,
    private val api: HuertoApi? = null
) : TareaRepository {

    override fun todas(): Flow<List<Tarea>> = dao.todas()

    override fun paraEstudiante(usuarioId: String, grupo: String?): Flow<List<Tarea>> =
        dao.paraEstudiante(usuarioId, grupo)

    override fun observar(id: Int): Flow<Tarea?> = dao.observar(id)

    override suspend fun porId(id: Int): Tarea? = dao.porId(id)

    override suspend fun crear(tarea: Tarea) {
        val delServidor = api?.let { intentarRemoto { it.crearTarea(tarea.toDto(conId = false)) } }?.toEntity()
        val usarServidor = delServidor != null && dao.porId(delServidor.id) == null
        dao.insertar(if (usarServidor) delServidor!! else tarea)
    }

    override suspend fun actualizar(tarea: Tarea) {
        dao.actualizar(tarea)
        api?.let { remoto ->
            // PUT cambia los datos; el estado se cambia con su propio endpoint (PATCH)
            intentarRemoto { remoto.actualizarTarea(tarea.id.toLong(), tarea.toDto()) }
            intentarRemoto { remoto.cambiarEstadoTarea(tarea.id.toLong(), EstadoTareaDto(tarea.estado)) }
        }
    }

    override suspend fun eliminar(tarea: Tarea) {
        dao.eliminar(tarea)
        api?.let { intentarRemoto { it.eliminarTarea(tarea.id.toLong()) } }
    }
}
