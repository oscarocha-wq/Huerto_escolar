package cl.duoc.huerto

import cl.duoc.huerto.data.entities.Evidencia
import cl.duoc.huerto.data.entities.Validacion
import cl.duoc.huerto.domain.*
import org.junit.Assert.*
import org.junit.Test

class SeguimientoTest {

    private fun evidencia(id: Int, fecha: Long) =
        Evidencia(id = id, tareaId = 1, autorId = "EST-01", fecha = fecha, comentario = "c$id", fotoRuta = null)

    private fun validacion(id: Int, evidenciaId: Int, resultado: ResultadoValidacion, fecha: Long) =
        Validacion(
            id = id, tareaId = 1, evidenciaId = evidenciaId, validadorId = "ADM-01",
            resultado = resultado, comentario = null, fecha = fecha
        )

    @Test fun `sin entregas no hay ultima evidencia ni revision`() {
        val s = Seguimiento()
        assertNull(s.ultimaEvidencia)
        assertNull(s.revisionActual)
        assertEquals(0, s.intentos)
    }

    @Test fun `la ultima evidencia es la mas reciente`() {
        val s = Seguimiento(listOf(evidencia(1, 100L), evidencia(2, 300L), evidencia(3, 200L)))
        assertEquals(2, s.ultimaEvidencia?.id)
        assertEquals(3, s.intentos)
    }

    @Test fun `tras un rechazo y reenvio la revision actual queda vacia`() {
        val s = Seguimiento(
            evidencias = listOf(evidencia(1, 100L), evidencia(2, 300L)),
            validaciones = listOf(validacion(1, 1, ResultadoValidacion.RECHAZADA, 200L))
        )
        assertNull(s.revisionActual)
        assertEquals(1, s.revisionesAnteriores.size)
    }

    @Test fun `la revision actual corresponde a la ultima entrega`() {
        val s = Seguimiento(
            evidencias = listOf(evidencia(1, 100L), evidencia(2, 300L)),
            validaciones = listOf(
                validacion(1, 1, ResultadoValidacion.RECHAZADA, 200L),
                validacion(2, 2, ResultadoValidacion.APROBADA, 400L)
            )
        )
        assertEquals(ResultadoValidacion.APROBADA, s.revisionActual?.resultado)
        assertEquals(listOf(1), s.revisionesAnteriores.map { it.id })
    }

    @Test fun `una entrega necesita foto o comentario`() {
        assertNotNull(validarEntrega("", null))
        assertNotNull(validarEntrega("   ", null))
        assertNull(validarEntrega("Regué el sector", null))
        assertNull(validarEntrega("", "/ruta/foto.jpg"))
    }

    @Test fun `el comentario de la entrega respeta el largo maximo`() {
        assertNotNull(validarEntrega("a".repeat(LimitesCampo.COMENTARIO + 1), null))
    }
}
