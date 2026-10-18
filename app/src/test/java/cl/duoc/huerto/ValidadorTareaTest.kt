package cl.duoc.huerto

import cl.duoc.huerto.domain.TipoTarea
import cl.duoc.huerto.domain.validarTarea
import org.junit.Assert.*
import org.junit.Test

class ValidadorTareaTest {
    @Test fun `formulario vacio genera todos los errores`() {
        val e = validarTarea(null, " ", null, null, false)
        assertNotNull(e.tipo)
        assertNotNull(e.descripcion)
        assertNotNull(e.sector)
        assertNotNull(e.fecha)
        assertNotNull(e.asignado)
    }

    @Test fun `formulario completo no genera errores`() {
        val e = validarTarea(TipoTarea.RIEGO, "Regar con regadera", 1, 1000L, true)
        assertFalse(e.hayErrores)
    }
}