package cl.duoc.huerto.data.repository

import cl.duoc.huerto.data.dao.UsuarioDao
import cl.duoc.huerto.domain.Sesion

class UsuarioRepository(private val dao: UsuarioDao) {

    suspend fun login(id: String, clave: String): Sesion? {
        val usuario = dao.login(id.trim().uppercase(), clave) ?: return null
        return Sesion(
            usuarioId = usuario.id,
            nombre = usuario.nombre,
            perfil = usuario.perfil,
            grupo = usuario.grupo
        )
    }
    fun estudiantes() = dao.estudiantes()
}
