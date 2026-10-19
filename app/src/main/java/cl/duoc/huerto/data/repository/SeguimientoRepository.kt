package cl.duoc.huerto.data.repository

import cl.duoc.huerto.data.dao.SeguimientoDao
import cl.duoc.huerto.data.entities.Evidencia
import cl.duoc.huerto.data.entities.Validacion
import kotlinx.coroutines.flow.Flow

class SeguimientoRepository(private val dao: SeguimientoDao) {
    fun evidencias(): Flow<List<Evidencia>> = dao.evidencias()
    fun validaciones(): Flow<List<Validacion>> = dao.validaciones()
    fun evidenciasDe(tareaId: Int): Flow<List<Evidencia>> = dao.evidenciasDe(tareaId)
    fun validacionesDe(tareaId: Int): Flow<List<Validacion>> = dao.validacionesDe(tareaId)
    suspend fun ultimaEvidencia(tareaId: Int): Evidencia? = dao.ultimaEvidencia(tareaId)
    suspend fun contarEvidencias(tareaId: Int): Int = dao.contarEvidencias(tareaId)
    suspend fun entregar(evidencia: Evidencia) = dao.entregar(evidencia)
    suspend fun registrarValidacion(validacion: Validacion) = dao.registrarValidacion(validacion)
}
