package cl.duoc.huerto.domain.repository

import cl.duoc.huerto.domain.model.Problema
import kotlinx.coroutines.flow.Flow

/** Contrato de acceso a problemas reportados. La implementación vive en la capa data. */
interface ProblemaRepository {
    fun todos(): Flow<List<Problema>>
    fun deUsuario(usuarioId: String): Flow<List<Problema>>
    fun observar(id: Int): Flow<Problema?>
    suspend fun porId(id: Int): Problema?
    suspend fun crear(problema: Problema)
    suspend fun actualizar(problema: Problema)
}
