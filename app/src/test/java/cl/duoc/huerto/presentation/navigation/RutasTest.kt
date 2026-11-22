package cl.duoc.huerto.presentation.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class RutasTest {

    @Test fun `las rutas con parametros arman el id`() {
        assertEquals("sector_form/3", Rutas.sectorForm(3))
        assertEquals("sector_detalle/4", Rutas.sectorDetalle(4))
        assertEquals("sensores/5", Rutas.sensores(5))
        assertEquals("cultivo_form/6", Rutas.cultivoForm(6))
        assertEquals("tarea_detalle/7", Rutas.tareaDetalle(7))
        assertEquals("problema_detalle/8", Rutas.problemaDetalle(8))
    }
}
