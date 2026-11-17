package cl.duoc.huerto.data.repository

import cl.duoc.huerto.data.local.dao.SeguimientoDao
import cl.duoc.huerto.domain.model.Evidencia
import cl.duoc.huerto.domain.model.Validacion
import cl.duoc.huerto.domain.repository.SeguimientoRepository
import kotlinx.coroutines.flow.Flow

class SeguimientoRepositoryImpl(private val dao: SeguimientoDao) : SeguimientoRepository {
    override fun evidencias(): Flow<List<Evidencia>> = dao.evidencias()
    override fun validaciones(): Flow<List<Validacion>> = dao.validaciones()
    override fun evidenciasDe(tareaId: Int): Flow<List<Evidencia>> = dao.evidenciasDe(tareaId)
    override fun validacionesDe(tareaId: Int): Flow<List<Validacion>> = dao.validacionesDe(tareaId)
    override suspend fun ultimaEvidencia(tareaId: Int): Evidencia? = dao.ultimaEvidencia(tareaId)
    override suspend fun contarEvidencias(tareaId: Int): Int = dao.contarEvidencias(tareaId)
    override suspend fun entregar(evidencia: Evidencia) = dao.entregar(evidencia)
    override suspend fun registrarValidacion(validacion: Validacion) = dao.registrarValidacion(validacion)
}
