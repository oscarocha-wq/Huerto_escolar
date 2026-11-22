package cl.duoc.huerto.data.remote

import cl.duoc.huerto.data.remote.clima.*
import cl.duoc.huerto.data.repository.ClimaRepositoryImpl
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

/** API externa del clima, con una API falsa (sin internet). */
private class FakeClimaApi(private val respuesta: ClimaRespuestaDto?) : ClimaApi {
    var latitudPedida = 0.0
    override suspend fun pronostico(
        latitud: Double, longitud: Double, actual: String, diario: String, zonaHoraria: String, dias: Int
    ): ClimaRespuestaDto {
        latitudPedida = latitud
        return respuesta ?: throw IOException("Sin internet")
    }
}

class ClimaTest {

    private val respuestaOk = ClimaRespuestaDto(
        current = ClimaActualDto(temperature_2m = 18.4, relative_humidity_2m = 55.0, precipitation = 0.2, weather_code = 3),
        daily = ClimaDiarioDto(precipitation_sum = listOf(1.5), temperature_2m_max = listOf(22.0), temperature_2m_min = listOf(9.0))
    )

    @Test fun `el JSON de Open-Meteo se convierte en Clima`() {
        val c = respuestaOk.toDomain()!!
        assertEquals(18.4, c.temperatura, 0.001)
        assertEquals(55, c.humedad)
        assertEquals(1.5, c.lluviaHoyMm, 0.001)
        assertEquals(22.0, c.maxima!!, 0.001)
        assertEquals(3, c.codigo)
    }

    @Test fun `sin datos diarios usa la lluvia actual y sin clima actual devuelve null`() {
        val c = respuestaOk.copy(daily = null).toDomain()!!
        assertEquals(0.2, c.lluviaHoyMm, 0.001)
        assertNull(c.maxima)
        assertNull(ClimaRespuestaDto(current = null, daily = null).toDomain())
        assertNull(ClimaRespuestaDto(ClimaActualDto(null, null, null, null), null).toDomain())
    }

    @Test fun `datos incompletos usan valores por defecto`() {
        val c = ClimaRespuestaDto(ClimaActualDto(10.0, null, null, null), null).toDomain()!!
        assertEquals(0, c.humedad)
        assertEquals(0.0, c.lluviaHoyMm, 0.001)
        assertEquals(-1, c.codigo)
    }

    @Test fun `el repositorio devuelve el clima de Santiago`() = runTest {
        val api = FakeClimaApi(respuestaOk)
        val r = ClimaRepositoryImpl(api).climaActual()
        assertTrue(r.isSuccess)
        assertEquals(-33.4489, api.latitudPedida, 0.0001)
    }

    @Test fun `sin internet o sin datos el repositorio devuelve error`() = runTest {
        assertTrue(ClimaRepositoryImpl(FakeClimaApi(null)).climaActual().isFailure)
        assertTrue(ClimaRepositoryImpl(FakeClimaApi(ClimaRespuestaDto(null, null))).climaActual().isFailure)
    }
}
