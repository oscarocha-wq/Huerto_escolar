package cl.duoc.huerto

import cl.duoc.huerto.domain.*
import cl.duoc.huerto.ui.theme.Tono
import cl.duoc.huerto.ui.theme.tono
import org.junit.Assert.*
import org.junit.Test

class ReglasTonosTest {

    @Test fun `requerido rechaza vacio y espacios`() {
        assertNotNull(Reglas.requerido("", "El nombre"))
        assertNotNull(Reglas.requerido("   ", "El nombre"))
        assertNull(Reglas.requerido("Sector Norte", "El nombre"))
    }

    @Test fun `largo respeta minimo y maximo`() {
        assertNotNull(Reglas.largo("ab", "El nombre", max = 40, min = 3))
        assertNotNull(Reglas.largo("x".repeat(41), "El nombre", max = 40))
        assertNull(Reglas.largo("Lechuga", "El nombre", max = 40, min = 3))
    }

    @Test fun `frecuencia de riego debe ser entero positivo`() {
        val max = LimitesCampo.RIEGO_MAX_DIAS
        assertNotNull(Reglas.enteroPositivo("", "Riego", max))
        assertNotNull(Reglas.enteroPositivo("abc", "Riego", max))
        assertNotNull(Reglas.enteroPositivo("0", "Riego", max))
        assertNotNull(Reglas.enteroPositivo("-2", "Riego", max))
        assertNotNull(Reglas.enteroPositivo("31", "Riego", max))
        assertNull(Reglas.enteroPositivo("3", "Riego", max))
    }

    @Test fun `motivo de rechazo exige minimo`() {
        val r = { m: String ->
            Reglas.requeridoYLargo(m, "El motivo", LimitesCampo.COMENTARIO, LimitesCampo.MOTIVO_RECHAZO_MIN)
        }
        assertNotNull(r(""))
        assertNotNull(r("mal"))
        assertNull(r("Falta la foto del riego"))
    }

    @Test fun `estados de tarea tienen el tono correcto`() {
        assertEquals(Tono.NEUTRO, EstadoTarea.PENDIENTE.tono())
        assertEquals(Tono.ADVERTENCIA, EstadoTarea.PENDIENTE_DE_VALIDACION.tono())
        assertEquals(Tono.EXITO, EstadoTarea.VALIDADA.tono())
        assertEquals(Tono.ERROR, EstadoTarea.RECHAZADA.tono())
    }

    @Test fun `severidad de alerta y problemas usan tonos coherentes`() {
        assertEquals(Tono.ERROR, SeveridadAlerta.URGENTE.tono())
        assertEquals(Tono.ADVERTENCIA, SeveridadAlerta.AVISO.tono())
        assertEquals(Tono.ERROR, EstadoProblema.INFORMADO.tono())
        assertEquals(Tono.EXITO, EstadoProblema.CERRADO.tono())
    }
}