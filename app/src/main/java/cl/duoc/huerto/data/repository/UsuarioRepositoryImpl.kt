package cl.duoc.huerto.data.repository

import cl.duoc.huerto.data.local.dao.UsuarioDao
import cl.duoc.huerto.domain.model.Sesion
import cl.duoc.huerto.domain.model.Usuario
import cl.duoc.huerto.domain.repository.UsuarioRepository
import kotlinx.coroutines.flow.Flow

class UsuarioRepositoryImpl(private val dao: UsuarioDao) : UsuarioRepository {

    override suspend fun login(id: String, clave: String): Sesion? {
        val usuario = dao.login(id.trim().uppercase(), clave) ?: return null
        return Sesion(
            usuarioId = usuario.id,
            nombre = usuario.nombre,
            perfil = usuario.perfil,
            grupo = usuario.grupo
        )
    }

    override fun estudiantes(): Flow<List<Usuario>> = dao.estudiantes()
}
