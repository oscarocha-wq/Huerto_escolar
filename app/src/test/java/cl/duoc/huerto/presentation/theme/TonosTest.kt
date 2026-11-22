package cl.duoc.huerto.presentation.theme

import cl.duoc.huerto.domain.model.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/** Cada estado del dominio se pinta con un tono del tema (sin colores fijos). */
class TonosTest {

    @Test fun `todos los valores tienen un tono`() {
        EstadoTarea.entries.forEach { assertNotNull(it.tono()) }
        EstadoCultivo.entries.forEach { assertNotNull(it.tono()) }
        EstadoProblema.entries.forEach { assertNotNull(it.tono()) }
        EstadoSector.entries.forEach { assertNotNull(it.tono()) }
        SeveridadAlerta.entries.forEach { assertNotNull(it.tono()) }
        TipoEvento.entries.forEach { assertNotNull(it.tono()) }
        EstadoLectura.entries.forEach { assertNotNull(it.tono()) }
    }

    @Test fun `tonos esperados`() {
        assertEquals(Tono.EXITO, EstadoLectura.NORMAL.tono())
        assertEquals(Tono.ERROR, SeveridadAlerta.URGENTE.tono())
    }
}
