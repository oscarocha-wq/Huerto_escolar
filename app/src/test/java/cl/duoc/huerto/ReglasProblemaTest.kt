package cl.duoc.huerto

import cl.duoc.huerto.domain.*
import org.junit.Assert.*
import org.junit.Test

class ReglasProblemaTest {
    private val admin = Sesion("ADM-01", "Docente Prueba", Perfil.ADMINISTRADOR, null)
    private val est1 = Sesion("EST-01", "Estudiante Uno", Perfil.ESTUDIANTE, "Taller A")
    private val est2 = Sesion("EST-02", "Estudiante Dos", Perfil.ESTUDIANTE, "Taller A")

    @Test fun `solo estudiantes reportan problemas`() {
        assertTrue(puedeReportar(est1))
        assertFalse(puedeReportar(admin))
        assertFalse(puedeReportar(null))
    }

    @Test fun `estudiante solo ve sus reportes y admin ve todos`() {
        assertTrue(puedeVerProblema(est1, "EST-01"))
        assertFalse(puedeVerProblema(est2, "EST-01"))
        assertTrue(puedeVerProblema(admin, "EST-01"))
        assertFalse(puedeVerProblema(null, "EST-01"))
    }

    @Test fun `solo admin gestiona el estado`() {
        assertTrue(puedeGestionarProblema(admin))
        assertFalse(puedeGestionarProblema(est1))
        assertFalse(puedeGestionarProblema(null))
    }

    @Test fun `transiciones de estado permitidas`() {
        assertEquals(listOf(EstadoProblema.EN_REVISION), siguientesEstados(EstadoProblema.INFORMADO))
        assertEquals(listOf(EstadoProblema.ATENDIDO), siguientesEstados(EstadoProblema.EN_REVISION))
        assertTrue(EstadoProblema.CERRADO in siguientesEstados(EstadoProblema.ATENDIDO))
        assertTrue(siguientesEstados(EstadoProblema.CERRADO).isEmpty())
    }

    @Test fun `no se puede saltar de informado a cerrado`() {
        assertFalse(EstadoProblema.CERRADO in siguientesEstados(EstadoProblema.INFORMADO))
    }

    @Test fun `atender un problema exige comentario`() {
        assertTrue(requiereComentario(EstadoProblema.ATENDIDO))
        assertFalse(requiereComentario(EstadoProblema.EN_REVISION))
    }
}