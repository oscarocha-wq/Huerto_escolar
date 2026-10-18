package cl.duoc.huerto

import cl.duoc.huerto.domain.ResumenAvisos
import cl.duoc.huerto.domain.mensaje
import org.junit.Assert.*
import org.junit.Test

class AvisosTest {
    @Test fun `sin novedades no hay aviso`() {
        assertNull(ResumenAvisos().mensaje(esAdmin = true))
        assertNull(ResumenAvisos().mensaje(esAdmin = false))
    }

    @Test fun `estudiante solo recibe aviso de atrasadas`() {
        val r = ResumenAvisos(atrasadas = 2, porValidar = 5, problemasNuevos = 3)
        assertEquals("2 tareas atrasadas", r.mensaje(esAdmin = false))
    }

    @Test fun `administrador recibe todos los avisos`() {
        val r = ResumenAvisos(atrasadas = 1, porValidar = 2, problemasNuevos = 1)
        assertEquals(
            "1 tarea atrasada · 2 tareas por validar · 1 problema sin revisar",
            r.mensaje(esAdmin = true)
        )
    }

    @Test fun `administrador sin atrasadas solo ve lo pendiente`() {
        val r = ResumenAvisos(porValidar = 1)
        assertEquals("1 tarea por validar", r.mensaje(esAdmin = true))
    }
}