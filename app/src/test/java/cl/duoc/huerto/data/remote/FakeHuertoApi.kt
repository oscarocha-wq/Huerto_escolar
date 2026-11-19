package cl.duoc.huerto.data.remote

import cl.duoc.huerto.data.remote.dto.*
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response
import java.io.IOException

/**
 * Backend falso para las pruebas: no usa red.
 * - sinConexion = true  -> simula que huertoapi está apagado (IOException)
 * - las listas se pueden cambiar en cada prueba
 */
class FakeHuertoApi(
    var sinConexion: Boolean = false,
    var usuarios: List<UsuarioDto> = emptyList(),
    var sectores: List<SectorDto> = emptyList(),
    var cultivos: List<CultivoDto> = emptyList(),
    var tareas: List<TareaDto> = emptyList(),
    var problemas: List<ProblemaDto> = emptyList(),
    private val claves: Map<String, String> = emptyMap()
) : HuertoApi {

    private fun revisarConexion() {
        if (sinConexion) throw IOException("Servidor apagado")
    }

    override suspend fun login(request: LoginRequestDto): Response<UsuarioDto> {
        revisarConexion()
        val usuario = usuarios.firstOrNull { it.id == request.id }
        return if (usuario != null && claves[request.id] == request.clave) {
            Response.success(usuario)
        } else {
            Response.error(401, "Usuario o clave incorrectos".toResponseBody(null))
        }
    }

    override suspend fun usuarios(): List<UsuarioDto> { revisarConexion(); return usuarios }
    override suspend fun sectores(): List<SectorDto> { revisarConexion(); return sectores }
    override suspend fun cultivos(): List<CultivoDto> { revisarConexion(); return cultivos }
    override suspend fun tareas(): List<TareaDto> { revisarConexion(); return tareas }
    override suspend fun problemas(): List<ProblemaDto> { revisarConexion(); return problemas }
}
