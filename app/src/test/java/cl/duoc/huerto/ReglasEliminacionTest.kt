package cl.duoc.huerto

import cl.duoc.huerto.domain.*
import org.junit.Assert.*
import org.junit.Test

class ReglasEliminacionTest {

    private val admin = Sesion("ADM-01", "Docente", Perfil.ADMINISTRADOR, null)
    private val estudiante = Sesion("EST-01", "Estudiante", Perfil.ESTUDIANTE, "Taller A")

    @Test fun `sector sin dependencias se puede eliminar`() {
        assertNull(motivoNoEliminarSector(DependenciasSector()))
    }

    @Test fun `sector con cultivos tareas o problemas no se elimina`() {
        val motivo = motivoNoEliminarSector(DependenciasSector(cultivos = 2, tareas = 1, problemas = 0))
        assertNotNull(motivo)
        assertTrue(motivo!!.contains("2 cultivos"))
        assertTrue(motivo.contains("1 tarea"))
        assertFalse(motivo.contains("problema"))
    }

    @Test fun `cultivo con tareas no se elimina`() {
        assertNull(motivoNoEliminarCultivo(0))
        assertNotNull(motivoNoEliminarCultivo(3))
    }

    @Test fun `solo el admin elimina tareas pendientes sin entregas`() {
        assertTrue(puedeEliminarTarea(admin, EstadoTarea.PENDIENTE, 0))
        assertFalse(puedeEliminarTarea(estudiante, EstadoTarea.PENDIENTE, 0))
        assertFalse(puedeEliminarTarea(null, EstadoTarea.PENDIENTE, 0))
        assertFalse(puedeEliminarTarea(admin, EstadoTarea.EN_DESARROLLO, 0))
        assertFalse(puedeEliminarTarea(admin, EstadoTarea.PENDIENTE, 1))
        assertFalse(puedeEliminarTarea(admin, EstadoTarea.VALIDADA, 1))
    }
}
