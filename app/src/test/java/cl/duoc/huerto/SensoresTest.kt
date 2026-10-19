package cl.duoc.huerto

import cl.duoc.huerto.domain.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class SensoresTest {

    @Test fun `umbrales clasifican bajo normal y alto`() {
        assertEquals(EstadoLectura.BAJO, Variable.HUMEDAD_SUELO.evaluar(20.0))
        assertEquals(EstadoLectura.NORMAL, Variable.HUMEDAD_SUELO.evaluar(50.0))
        assertEquals(EstadoLectura.ALTO, Variable.HUMEDAD_SUELO.evaluar(90.0))
        assertEquals(EstadoLectura.ALTO, Variable.TEMPERATURA.evaluar(35.0))
    }

    @Test fun `acepta coma o punto decimal`() {
        assertEquals(22.5, parseNumero("22,5")!!, 0.0001)
        assertEquals(22.5, parseNumero(" 22.5 ")!!, 0.0001)
        assertNull(parseNumero("abc"))
    }

    @Test fun `la simulacion queda marcada como dato de prueba y dentro de rango`() {
        repeat(50) { i ->
            val l = simularLectura(sectorId = 1, ahora = 1000L, random = Random(i))
            assertEquals(OrigenLectura.SIMULADA, l.origen)
            assertEquals(1, l.sectorId)
            assertTrue(l.humedadSuelo in Variable.HUMEDAD_SUELO.min..Variable.HUMEDAD_SUELO.max)
            assertTrue(l.temperatura in Variable.TEMPERATURA.min..Variable.TEMPERATURA.max)
            assertTrue(l.humedadAmbiente in Variable.HUMEDAD_AMBIENTE.min..Variable.HUMEDAD_AMBIENTE.max)
        }
    }

    @Test fun `registro manual valido no tiene errores`() {
        assertFalse(validarLectura("45", "18,5", "60").hayErrores)
    }

    @Test fun `registro manual detecta vacios texto y fuera de rango`() {
        val e = validarLectura("", "abc", "150")
        assertNotNull(e.humedadSuelo)
        assertNotNull(e.temperatura)
        assertNotNull(e.humedadAmbiente)
    }
}
