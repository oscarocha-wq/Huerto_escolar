package cl.duoc.huerto.domain.usecase

import cl.duoc.huerto.domain.model.Clima
import org.junit.Assert.assertEquals
import org.junit.Test

class ClimaReglasTest {

    private fun clima(temp: Double = 20.0, humedad: Int = 60, lluvia: Double = 0.0) =
        Clima(temperatura = temp, humedad = humedad, lluviaHoyMm = lluvia, maxima = null, minima = null, codigo = 0)

    @Test fun `traduce los codigos del clima`() {
        assertEquals("Despejado", descripcionClima(0))
        assertEquals("Parcialmente nublado", descripcionClima(2))
        assertEquals("Nublado", descripcionClima(3))
        assertEquals("Neblina", descripcionClima(45))
        assertEquals("Llovizna", descripcionClima(53))
        assertEquals("Lluvia", descripcionClima(63))
        assertEquals("Nieve", descripcionClima(73))
        assertEquals("Chubascos", descripcionClima(81))
        assertEquals("Tormenta", descripcionClima(95))
        assertEquals("Sin información", descripcionClima(-1))
    }

    @Test fun `consejo de riego segun el clima`() {
        assertEquals("Hoy llueve lo suficiente: no es necesario regar.", consejoRiego(clima(lluvia = 8.0)))
        assertEquals("Hace mucho calor: rieguen temprano o al atardecer.", consejoRiego(clima(temp = 33.0)))
        assertEquals("Ambiente muy seco: revisen la humedad del suelo.", consejoRiego(clima(humedad = 20)))
        assertEquals("Rieguen según la frecuencia de cada cultivo.", consejoRiego(clima()))
    }
}
