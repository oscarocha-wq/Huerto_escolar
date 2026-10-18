package cl.duoc.huerto

import cl.duoc.huerto.domain.formatearFechaUtc
import cl.duoc.huerto.domain.validarCultivo
import org.junit.Assert.*
import org.junit.Test

class ValidadorCultivoTest {
    private val dia = 86_400_000L

    @Test fun `campos obligatorios vacios generan errores`() {
        val e = validarCultivo("", null, "", null, null, null)
        assertNotNull(e.nombre)
        assertNotNull(e.sector)
        assertNotNull(e.frecuencia)
        assertTrue(e.hayErrores)
    }

    @Test fun `datos validos no generan errores`() {
        val e = validarCultivo("Lechuga", 1, "2", 10 * dia, null, 40 * dia)
        assertFalse(e.hayErrores)
    }

    @Test fun `frecuencia fuera de rango es invalida`() {
        assertNotNull(validarCultivo("Lechuga", 1, "0", null, null, null).frecuencia)
        assertNotNull(validarCultivo("Lechuga", 1, "99", null, null, null).frecuencia)
    }

    @Test fun `cosecha anterior a siembra es invalida`() {
        val e = validarCultivo("Lechuga", 1, "2", 40 * dia, null, 10 * dia)
        assertNotNull(e.cosecha)
    }

    @Test fun `fecha se formatea en UTC sin desfase`() {
        assertEquals("01/01/1970", formatearFechaUtc(0L))
        assertEquals("", formatearFechaUtc(null))
    }
}