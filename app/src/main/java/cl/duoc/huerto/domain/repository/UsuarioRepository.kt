package cl.duoc.huerto.domain.repository

import cl.duoc.huerto.domain.model.Sesion
import cl.duoc.huerto.domain.model.Usuario
import kotlinx.coroutines.flow.Flow

/** Contrato de autenticación y consulta de usuarios. La implementación vive en la capa data. */
interface UsuarioRepository {
    suspend fun login(id: String, clave: String): Sesion?
    fun estudiantes(): Flow<List<Usuario>>
}
