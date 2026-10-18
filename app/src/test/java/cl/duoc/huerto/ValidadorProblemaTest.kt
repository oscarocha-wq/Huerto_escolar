package cl.duoc.huerto

import cl.duoc.huerto.domain.MAX_DESCRIPCION_PROBLEMA
import cl.duoc.huerto.domain.TipoProblema
import cl.duoc.huerto.domain.validarProblema
import org.junit.Assert.*
import org.junit.Test

class ValidadorProblemaTest {
    @Test fun `formulario vacio genera todos los errores`() {
        val e = validarProblema(null, "  ", null)
        assertNotNull(e.tipo)
        assertNotNull(e.descripcion)
        assertNotNull(e.sector)
    }

    @Test fun `descripcion muy corta es invalida`() {
        assertNotNull(validarProblema(TipoProblema.FALTA_AGUA, "seco", 1).descripcion)
    }

    @Test fun `descripcion demasiado larga es invalida`() {
        val larga = "a".repeat(MAX_DESCRIPCION_PROBLEMA + 1)
        assertNotNull(validarProblema(TipoProblema.OTRO, larga, 1).descripcion)
    }

    @Test fun `reporte completo no genera errores`() {
        val e = validarProblema(TipoProblema.DANO_PLANTA, "Las hojas están amarillas", 1)
        assertFalse(e.hayErrores)
    }
}