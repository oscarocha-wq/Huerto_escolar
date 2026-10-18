package cl.duoc.huerto

import cl.duoc.huerto.domain.*
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class ReglasTareaTest {
    private val dia = 86_400_000L
    private val hoy = inicioDeHoyUtc(LocalDate.of(2026, 10, 16))

    private val admin = Sesion("ADM-01", "Docente Prueba", Perfil.ADMINISTRADOR, null)
    private val est1 = Sesion("EST-01", "Estudiante Uno", Perfil.ESTUDIANTE, "Taller A")
    private val est3 = Sesion("EST-03", "Estudiante Tres", Perfil.ESTUDIANTE, "Taller B")

    @Test fun `hoy se calcula a medianoche UTC sin desfase`() {
        assertEquals("16/10/2026", formatearFechaUtc(hoy))
    }

    @Test fun `tarea pendiente con fecha pasada esta atrasada`() {
        assertTrue(estaAtrasada(EstadoTarea.PENDIENTE, hoy - dia, hoy))
        assertTrue(estaAtrasada(EstadoTarea.RECHAZADA, hoy - dia, hoy))
    }

    @Test fun `tarea de hoy o futura no esta atrasada`() {
        assertFalse(estaAtrasada(EstadoTarea.PENDIENTE, hoy, hoy))
        assertFalse(estaAtrasada(EstadoTarea.PENDIENTE, hoy + dia, hoy))
    }

    @Test fun `tarea enviada o validada no se considera atrasada`() {
        assertFalse(estaAtrasada(EstadoTarea.PENDIENTE_DE_VALIDACION, hoy - dia, hoy))
        assertFalse(estaAtrasada(EstadoTarea.VALIDADA, hoy - dia, hoy))
    }

    @Test fun `asignacion individual y por grupo`() {
        assertTrue(estaAsignadaA(est1, "EST-01", null))
        assertTrue(estaAsignadaA(est1, null, "Taller A"))
        assertFalse(estaAsignadaA(est1, "EST-02", null))
        assertFalse(estaAsignadaA(est3, null, "Taller A"))
    }

    @Test fun `estudiante no ve tareas de otros pero admin ve todas`() {
        assertFalse(puedeVerTarea(est3, "EST-01", null))
        assertTrue(puedeVerTarea(admin, "EST-01", null))
        assertFalse(puedeVerTarea(null, "EST-01", null))
    }

    @Test fun `solo admin puede revisar tareas pendientes de validacion`() {
        assertTrue(puedeRevisar(admin, EstadoTarea.PENDIENTE_DE_VALIDACION, "EST-01"))
        assertFalse(puedeRevisar(est1, EstadoTarea.PENDIENTE_DE_VALIDACION, "EST-01"))
        assertFalse(puedeRevisar(admin, EstadoTarea.PENDIENTE, null))
        assertFalse(puedeRevisar(admin, EstadoTarea.VALIDADA, "EST-01"))
    }

    @Test fun `nadie valida su propia tarea`() {
        assertFalse(puedeRevisar(admin, EstadoTarea.PENDIENTE_DE_VALIDACION, "ADM-01"))
    }

    @Test fun `tarea rechazada puede repetirse pero la validada no`() {
        assertTrue(puedeMarcarRealizada(EstadoTarea.RECHAZADA))
        assertFalse(puedeMarcarRealizada(EstadoTarea.VALIDADA))
        assertFalse(puedeMarcarRealizada(EstadoTarea.PENDIENTE_DE_VALIDACION))
    }
}
