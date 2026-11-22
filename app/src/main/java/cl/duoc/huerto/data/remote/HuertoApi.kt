package cl.duoc.huerto.data.remote

import cl.duoc.huerto.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Endpoints del microservicio huertoapi (Spring Boot, puerto 8080).
 * Retrofit genera la implementación de esta interfaz.
 */
interface HuertoApi {

    // ---------- Usuarios ----------
    // POST /usuarios/login  -> 200 con el usuario, o 401 si la clave es incorrecta
    @POST("usuarios/login")
    suspend fun login(@Body request: LoginRequestDto): Response<UsuarioDto>

    @GET("usuarios")
    suspend fun usuarios(): List<UsuarioDto>

    // ---------- Sectores (CRUD) ----------
    @GET("sectores")
    suspend fun sectores(): List<SectorDto>

    @POST("sectores")
    suspend fun crearSector(@Body sector: SectorDto): SectorDto

    @PUT("sectores/{id}")
    suspend fun actualizarSector(@Path("id") id: Long, @Body sector: SectorDto): SectorDto

    @DELETE("sectores/{id}")
    suspend fun eliminarSector(@Path("id") id: Long): Response<Unit>

    // ---------- Cultivos (CRUD) ----------
    @GET("cultivos")
    suspend fun cultivos(): List<CultivoDto>

    @POST("cultivos")
    suspend fun crearCultivo(@Body cultivo: CultivoDto): CultivoDto

    @PUT("cultivos/{id}")
    suspend fun actualizarCultivo(@Path("id") id: Long, @Body cultivo: CultivoDto): CultivoDto

    @DELETE("cultivos/{id}")
    suspend fun eliminarCultivo(@Path("id") id: Long): Response<Unit>

    // ---------- Tareas (CRUD + cambio de estado) ----------
    @GET("tareas")
    suspend fun tareas(): List<TareaDto>

    @POST("tareas")
    suspend fun crearTarea(@Body tarea: TareaDto): TareaDto

    @PUT("tareas/{id}")
    suspend fun actualizarTarea(@Path("id") id: Long, @Body tarea: TareaDto): TareaDto

    @PATCH("tareas/{id}/estado")
    suspend fun cambiarEstadoTarea(@Path("id") id: Long, @Body estado: EstadoTareaDto): TareaDto

    @DELETE("tareas/{id}")
    suspend fun eliminarTarea(@Path("id") id: Long): Response<Unit>

    // ---------- Problemas (crear + atender) ----------
    @GET("problemas")
    suspend fun problemas(): List<ProblemaDto>

    @POST("problemas")
    suspend fun crearProblema(@Body problema: ProblemaDto): ProblemaDto

    @PATCH("problemas/{id}/estado")
    suspend fun atenderProblema(@Path("id") id: Long, @Body atencion: AtencionProblemaDto): ProblemaDto
}
