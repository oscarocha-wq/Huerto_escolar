package cl.duoc.huerto

import cl.duoc.huerto.domain.LimitesCampo
import cl.duoc.huerto.domain.validarSector
import org.junit.Assert.*
import org.junit.Test

class ValidadorSectorTest {
    @Test fun `campos obligatorios vacios generan errores`() {
        val e = validarSector("", "  ", "")
        assertNotNull(e.codigo)
        assertNotNull(e.nombre)
        assertNotNull(e.ubicacion)
        assertTrue(e.hayErrores)
    }

    @Test fun `datos completos no generan errores`() {
        val e = validarSector("S-03", "Sector Este", "Junto al portón")
        assertFalse(e.hayErrores)
    }

    @Test fun `solo nombre vacio marca solo el nombre`() {
        val e = validarSector("S-03", "", "Junto al portón")
        assertNull(e.codigo)
        assertNotNull(e.nombre)
        assertNull(e.ubicacion)
    }

    @Test fun `codigo con formato incorrecto es invalido`() {
        assertNotNull(validarSector("sector3", "Sector Este", "Patio").codigo)
        assertNotNull(validarSector("S03", "Sector Este", "Patio").codigo)
        assertNull(validarSector("s-3", "Sector Este", "Patio").codigo)
    }

    @Test fun `nombre demasiado largo es invalido`() {
        val largo = "x".repeat(LimitesCampo.SECTOR_NOMBRE + 1)
        assertNotNull(validarSector("S-03", largo, "Patio").nombre)
    }

    @Test fun `descripcion es opcional pero tiene maximo`() {
        assertNull(validarSector("S-03", "Sector Este", "Patio", "").descripcion)
        val larga = "x".repeat(LimitesCampo.SECTOR_DESCRIPCION + 1)
        assertNotNull(validarSector("S-03", "Sector Este", "Patio", larga).descripcion)
    }
}