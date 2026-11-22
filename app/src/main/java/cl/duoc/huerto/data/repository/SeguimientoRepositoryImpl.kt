package cl.duoc.huerto.data.repository

import cl.duoc.huerto.data.local.dao.SeguimientoDao
import cl.duoc.huerto.data.remote.HuertoApi
import cl.duoc.huerto.data.remote.dto.EstadoTareaDto
import cl.duoc.huerto.data.remote.intentarRemoto
import cl.duoc.huerto.domain.model.EstadoTarea
import cl.duoc.huerto.domain.model.Evidencia
import cl.duoc.huerto.domain.model.ResultadoValidacion
import cl.duoc.huerto.domain.model.Validacion
import cl.duoc.huerto.domain.repository.SeguimientoRepository
import kotlinx.coroutines.flow.Flow

/**
 * Evidencias y validaciones quedan en Room (con sus fotos).
 * Al microservicio se le avisa el nuevo estado de la tarea (PATCH /tareas/{id}/estado).
 */
class SeguimientoRepositoryImpl(
    private val dao: SeguimientoDao,
    private val api: HuertoApi? = null
) : SeguimientoRepository {

    override fun evidencias(): Flow<List<Evidencia>> = dao.evidencias()

    override fun validaciones(): Flow<List<Validacion>> = dao.validaciones()

    override fun evidenciasDe(tareaId: Int): Flow<List<Evidencia>> = dao.evidenciasDe(tareaId)

    override fun validacionesDe(tareaId: Int): Flow<List<Validacion>> = dao.validacionesDe(tareaId)

    override suspend fun ultimaEvidencia(tareaId: Int): Evidencia? = dao.ultimaEvidencia(tareaId)

    override suspend fun contarEvidencias(tareaId: Int): Int = dao.contarEvidencias(tareaId)

    override suspend fun entregar(evidencia: Evidencia) {
        dao.entregar(evidencia)
        avisarEstado(evidencia.tareaId, EstadoTarea.PENDIENTE_DE_VALIDACION)
    }

    override suspend fun registrarValidacion(validacion: Validacion) {
        dao.registrarValidacion(validacion)
        val estado = if (validacion.resultado == ResultadoValidacion.APROBADA) EstadoTarea.VALIDADA
                     else EstadoTarea.RECHAZADA
        avisarEstado(validacion.tareaId, estado)
    }

    private suspend fun avisarEstado(tareaId: Int, estado: EstadoTarea) {
        api?.let { intentarRemoto { it.cambiarEstadoTarea(tareaId.toLong(), EstadoTareaDto(estado)) } }
    }
}
