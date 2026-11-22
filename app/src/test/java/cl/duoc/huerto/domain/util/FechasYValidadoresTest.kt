package cl.duoc.huerto.domain.util

import cl.duoc.huerto.domain.validation.validarEntrega
import cl.duoc.huerto.domain.validation.validarLectura
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class FechasYValidadoresTest {

    @Test fun `formatea fechas del DatePicker en UTC`() {
        assertEquals("", formatearFechaUtc(null))
        assertEquals("01/01/1970", formatearFechaUtc(0L))
        assertEquals("02/01/1970", formatearFechaUtc(86_400_000L))
    }

    @Test fun `inicio de hoy es medianoche UTC`() {
        assertEquals(86_400_000L, inicioDeHoyUtc(LocalDate.of(1970, 1, 2)))
        assertEquals(0L, inicioDeHoyUtc() % 86_400_000L)
    }

    @Test fun `formatea fecha y hora`() {
        assertTrue(formatearFechaHora(0L).matches(Regex("\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}")))
    }

    @Test fun `una entrega necesita foto o comentario`() {
        assertNotNull(validarEntrega("   ", null))
        assertNull(validarEntrega("", "foto.jpg"))
        assertNull(validarEntrega("Listo", null))
        assertNotNull(validarEntrega("x".repeat(301), null))
    }

    @Test fun `lectura valida y con errores`() {
        assertFalse(validarLectura("40", "20,5", "60.0").hayErrores)
        val e = validarLectura("", "abc", "120")
        assertEquals("Ingresa un valor", e.humedadSuelo)
        assertNotNull(e.temperatura)
        assertTrue(e.humedadAmbiente!!.contains("entre 0 y 100"))
        assertTrue(validarLectura("40", "-20", "50").temperatura!!.contains("entre -10 y 50"))
    }
}
