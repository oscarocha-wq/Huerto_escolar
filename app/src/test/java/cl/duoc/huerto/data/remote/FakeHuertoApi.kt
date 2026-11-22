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

    /** Cada llamada de escritura queda anotada, ej. "POST sectores", "PATCH tareas/3/estado". */
    val llamadas = mutableListOf<String>()
    /** Id que el "servidor" asigna al crear. */
    var proximoId = 100L

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

    // ---------- Escrituras (CRUD) ----------
    private fun anotar(llamada: String) { revisarConexion(); llamadas += llamada }

    override suspend fun crearSector(sector: SectorDto): SectorDto { anotar("POST sectores"); return sector.copy(id = proximoId) }
    override suspend fun actualizarSector(id: Long, sector: SectorDto): SectorDto { anotar("PUT sectores/$id"); return sector }
    override suspend fun eliminarSector(id: Long): Response<Unit> { anotar("DELETE sectores/$id"); return Response.success(Unit) }

    override suspend fun crearCultivo(cultivo: CultivoDto): CultivoDto { anotar("POST cultivos"); return cultivo.copy(id = proximoId, fotoUri = null) }
    override suspend fun actualizarCultivo(id: Long, cultivo: CultivoDto): CultivoDto { anotar("PUT cultivos/$id"); return cultivo }
    override suspend fun eliminarCultivo(id: Long): Response<Unit> { anotar("DELETE cultivos/$id"); return Response.success(Unit) }

    override suspend fun crearTarea(tarea: TareaDto): TareaDto { anotar("POST tareas"); return tarea.copy(id = proximoId) }
    override suspend fun actualizarTarea(id: Long, tarea: TareaDto): TareaDto { anotar("PUT tareas/$id"); return tarea }
    override suspend fun cambiarEstadoTarea(id: Long, estado: EstadoTareaDto): TareaDto {
        anotar("PATCH tareas/$id/estado ${estado.estado}")
        return TareaDto(id, null, null, 1, null, null, null, null, null, "ADM-01", estado.estado)
    }
    override suspend fun eliminarTarea(id: Long): Response<Unit> { anotar("DELETE tareas/$id"); return Response.success(Unit) }

    override suspend fun crearProblema(problema: ProblemaDto): ProblemaDto { anotar("POST problemas"); return problema.copy(id = proximoId, fotoRuta = null) }
    override suspend fun atenderProblema(id: Long, atencion: AtencionProblemaDto): ProblemaDto {
        anotar("PATCH problemas/$id/estado ${atencion.estado}")
        return ProblemaDto(id, null, null, 1, null, "EST-01", null, null, atencion.estado, atencion.comentarioDocente, atencion.atendidoPorId, null)
    }
}
