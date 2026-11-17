package cl.duoc.huerto.domain.repository

import cl.duoc.huerto.domain.model.Evidencia
import cl.duoc.huerto.domain.model.Validacion
import kotlinx.coroutines.flow.Flow

/** Contrato de entregas (evidencias) y revisiones (validaciones) de tareas. */
interface SeguimientoRepository {
    fun evidencias(): Flow<List<Evidencia>>
    fun validaciones(): Flow<List<Validacion>>
    fun evidenciasDe(tareaId: Int): Flow<List<Evidencia>>
    fun validacionesDe(tareaId: Int): Flow<List<Validacion>>
    suspend fun ultimaEvidencia(tareaId: Int): Evidencia?
    suspend fun contarEvidencias(tareaId: Int): Int
    suspend fun entregar(evidencia: Evidencia)
    suspend fun registrarValidacion(validacion: Validacion)
}
