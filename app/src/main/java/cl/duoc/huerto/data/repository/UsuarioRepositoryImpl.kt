package cl.duoc.huerto.data.repository

import cl.duoc.huerto.data.local.dao.UsuarioDao
import cl.duoc.huerto.data.remote.HuertoApi
import cl.duoc.huerto.data.remote.dto.LoginRequestDto
import cl.duoc.huerto.domain.model.Sesion
import cl.duoc.huerto.domain.model.Usuario
import cl.duoc.huerto.domain.repository.UsuarioRepository
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.cancellation.CancellationException

/**
 * Login en dos pasos:
 * 1) Si hay api, pregunta al backend. 200 = entra (y se guarda en Room para entrar
 *    sin conexión la próxima vez); 401 = clave incorrecta.
 * 2) Si el servidor no responde (apagado, sin red), valida contra Room como siempre.
 * Con api = null (por ejemplo en las pruebas unitarias) funciona solo con Room.
 */
class UsuarioRepositoryImpl(
    private val dao: UsuarioDao,
    private val api: HuertoApi? = null
) : UsuarioRepository {

    override suspend fun login(id: String, clave: String): Sesion? {
        val codigo = id.trim().uppercase()

        if (api != null) {
            try {
                val respuesta = api.login(LoginRequestDto(codigo, clave))
                if (respuesta.isSuccessful) {
                    val usuario = respuesta.body()?.toEntity(clave)
                    if (usuario != null) {
                        dao.guardar(usuario)
                        return usuario.aSesion()
                    }
                } else if (respuesta.code() == 401) {
                    return null // el servidor dijo: usuario o clave incorrectos
                }
                // Otro error del servidor (500, etc.): se intenta con los datos locales
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Sin conexión con el backend (o respuesta inválida): se sigue con Room
            }
        }

        return dao.login(codigo, clave)?.aSesion()
    }

    override fun estudiantes(): Flow<List<Usuario>> = dao.estudiantes()

    private fun Usuario.aSesion() = Sesion(
        usuarioId = id,
        nombre = nombre,
        perfil = perfil,
        grupo = grupo
    )
}
