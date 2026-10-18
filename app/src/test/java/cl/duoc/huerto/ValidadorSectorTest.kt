package cl.duoc.huerto

import cl.duoc.huerto.domain.validarSector
import org.junit.Assert.*
import org.junit.Test

class ValidadorSectorTest {
    @Test fun `codigo y nombre vacios generan errores`() {
        val e = validarSector("", "  ")
        assertNotNull(e.codigo)
        assertNotNull(e.nombre)
        assertTrue(e.hayErrores)
    }

    @Test fun `datos completos no generan errores`() {
        val e = validarSector("S-03", "Sector Este")
        assertFalse(e.hayErrores)
    }

    @Test fun `solo nombre vacio marca solo el nombre`() {
        val e = validarSector("S-03", "")
        assertNull(e.codigo)
        assertNotNull(e.nombre)
    }
}