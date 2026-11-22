package cl.duoc.huerto.domain.model

import org.junit.Assert.*
import org.junit.Test

/** Todos los valores de los enums tienen una etiqueta legible para la interfaz. */
class EtiquetasYModelosTest {

    @Test fun `cada estado y tipo tiene etiqueta`() {
        EstadoSector.entries.forEach { assertTrue(it.etiqueta().isNotBlank()) }
        EstadoCultivo.entries.forEach { assertTrue(it.etiqueta().isNotBlank()) }
        EstadoTarea.entries.forEach { assertTrue(it.etiqueta().isNotBlank()) }
        TipoTarea.entries.forEach { assertTrue(it.etiqueta().isNotBlank()) }
        TipoProblema.entries.forEach { assertTrue(it.etiqueta().isNotBlank()) }
        EstadoProblema.entries.forEach { assertTrue(it.etiqueta().isNotBlank()) }
        EstadoLectura.entries.forEach { assertTrue(it.etiqueta().isNotBlank()) }
        OrigenLectura.entries.forEach { assertTrue(it.etiqueta().isNotBlank()) }
        ResultadoValidacion.entries.forEach { assertTrue(it.etiqueta().isNotBlank()) }
    }

    @Test fun `algunas etiquetas concretas`() {
        assertEquals("Fuera de uso", EstadoSector.FUERA_DE_USO.etiqueta())
        assertEquals("Pendiente de validación", EstadoTarea.PENDIENTE_DE_VALIDACION.etiqueta())
        assertEquals("Dato de prueba", OrigenLectura.SIMULADA.etiqueta())
        assertEquals("Rechazada", ResultadoValidacion.RECHAZADA.etiqueta())
    }

    @Test fun `la sesion sabe si es administrador`() {
        assertTrue(Sesion("ADM-01", "Docente", Perfil.ADMINISTRADOR, null).esAdministrador)
        assertFalse(Sesion("EST-01", "Ana", Perfil.ESTUDIANTE, "Taller A").esAdministrador)
    }

    @Test fun `resultado de sincronizacion suma el total`() {
        assertEquals(15, ResultadoSincronizacion(1, 2, 3, 4, 5).total)
    }
}
