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
 * 1) Si hay api, pregunta al backend. Si responde 200 con el usuario, entra
 *    (y se guarda en Room para poder entrar sin conexión la próxima vez).
 * 2) En cualquier otro caso (servidor apagado, sin red, 401, 500, u otro programa
 *    respondiendo en el puerto 8080) se valida contra Room, como siempre.
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
                val usuario = if (respuesta.isSuccessful) respuesta.body()?.toEntity(clave) else null
                if (usuario != null) {
                    dao.guardar(usuario)
                    return usuario.aSesion()
                }
                // El servidor no confirmó: se revisa con los datos del teléfono
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
