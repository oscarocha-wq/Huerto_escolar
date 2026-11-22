package cl.duoc.huerto.data.remote.clima

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * API EXTERNA (no es nuestra): Open-Meteo, pronóstico del tiempo gratis y sin clave.
 * Ejemplo: https://api.open-meteo.com/v1/forecast?latitude=-33.45&longitude=-70.66&current=temperature_2m
 */
interface ClimaApi {
    @GET("v1/forecast")
    suspend fun pronostico(
        @Query("latitude") latitud: Double,
        @Query("longitude") longitud: Double,
        @Query("current") actual: String = "temperature_2m,relative_humidity_2m,precipitation,weather_code",
        @Query("daily") diario: String = "precipitation_sum,temperature_2m_max,temperature_2m_min",
        @Query("timezone") zonaHoraria: String = "auto",
        @Query("forecast_days") dias: Int = 1
    ): ClimaRespuestaDto
}
