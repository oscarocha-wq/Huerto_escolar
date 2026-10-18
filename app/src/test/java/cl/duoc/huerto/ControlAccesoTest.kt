package cl.duoc.huerto

import cl.duoc.huerto.domain.Perfil
import cl.duoc.huerto.domain.Sesion
import cl.duoc.huerto.domain.puedeAcceder
import org.junit.Assert.*
import org.junit.Test

class ControlAccesoTest {
    private val admin = Sesion("ADM-01", "Docente Prueba", Perfil.ADMINISTRADOR, null)
    private val estudiante = Sesion("EST-01", "Estudiante Uno", Perfil.ESTUDIANTE, "Taller A")

    @Test fun `administrador accede a rutas de administrador`() =
        assertTrue(admin.puedeAcceder(Perfil.ADMINISTRADOR))

    @Test fun `estudiante no accede a rutas de administrador`() =
        assertFalse(estudiante.puedeAcceder(Perfil.ADMINISTRADOR))

    @Test fun `ruta compartida permite a cualquier sesion`() {
        assertTrue(admin.puedeAcceder(null))
        assertTrue(estudiante.puedeAcceder(null))
    }

    @Test fun `sin sesion no se accede a nada`() {
        val sinSesion: Sesion? = null
        assertFalse(sinSesion.puedeAcceder(null))
    }
}