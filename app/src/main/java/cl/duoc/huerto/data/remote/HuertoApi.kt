package cl.duoc.huerto.data.remote

import cl.duoc.huerto.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Endpoints del backend huertoapi (Spring Boot, puerto 8080).
 * Retrofit genera la implementación de esta interfaz.
 */
interface HuertoApi {

    // POST /usuarios/login  -> 200 con el usuario, o 401 si la clave es incorrecta
    @POST("usuarios/login")
    suspend fun login(@Body request: LoginRequestDto): Response<UsuarioDto>

    @GET("usuarios")
    suspend fun usuarios(): List<UsuarioDto>

    @GET("sectores")
    suspend fun sectores(): List<SectorDto>

    @GET("cultivos")
    suspend fun cultivos(): List<CultivoDto>

    @GET("tareas")
    suspend fun tareas(): List<TareaDto>

    @GET("problemas")
    suspend fun problemas(): List<ProblemaDto>
}
