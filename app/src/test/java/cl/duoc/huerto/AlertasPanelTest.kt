package cl.duoc.huerto

import cl.duoc.huerto.data.entities.*
import cl.duoc.huerto.domain.*
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class AlertasPanelTest {
    private val dia = DIA_MS
    private val hoy = inicioDeHoyUtc(LocalDate.of(2026, 10, 16))

    private val admin = Sesion("ADM-01", "Docente Prueba", Perfil.ADMINISTRADOR, null)
    private val est1 = Sesion("EST-01", "Estudiante Uno", Perfil.ESTUDIANTE, "Taller A")
    private val est3 = Sesion("EST-03", "Estudiante Tres", Perfil.ESTUDIANTE, "Taller B")

    private val sector = Sector(
        id = 1, codigo = "S-01", nombre = "Sector Norte",
        descripcion = "", ubicacion = "", responsableId = "ADM-01"
    )

    private fun cultivo(
        siembra: Long?, frecuencia: Int = 2, cosecha: Long? = null,
        estado: EstadoCultivo = EstadoCultivo.SEMBRADO
    ) = Cultivo(
        id = 1, nombre = "Lechuga", descripcion = "", sectorId = 1,
        fechaSiembra = siembra, fechaCosechaEstimada = cosecha,
        frecuenciaRiegoDias = frecuencia, estado = estado
    )

    private fun tarea(
        id: Int = 1, tipo: TipoTarea = TipoTarea.OBSERVACION,
        estado: EstadoTarea = EstadoTarea.PENDIENTE, fecha: Long = hoy,
        asignado: String = "EST-01", cultivoId: Int? = null
    ) = Tarea(
        id = id, tipo = tipo, descripcion = "x", sectorId = 1, cultivoId = cultivoId,
        fechaCreacion = 100L, fechaProgramada = fecha, asignadoId = asignado,
        grupo = null, creadaPorId = "ADM-01", estado = estado
    )

    private fun problema(estado: EstadoProblema = EstadoProblema.INFORMADO) = Problema(
        id = 1, tipo = TipoProblema.FALTA_AGUA, descripcion = "Seco", sectorId = 1,
        cultivoId = null, reportadoPorId = "EST-01", fechaReporte = 500L,
        fotoRuta = null, estado = estado
    )

    private fun tipos(a: List<Alerta>) = a.map { it.tipo }

    @Test fun `proximo riego suma la frecuencia`() {
        assertEquals(hoy + 3 * dia, proximoRiego(hoy, 3))
        assertNull(proximoRiego(null, 3))
        assertNull(proximoRiego(hoy, 0))
    }

    @Test fun `tarea atrasada genera alerta urgente`() {
        val a = generarAlertas(est1, listOf(tarea(fecha = hoy - dia)), emptyList(), listOf(sector), emptyList(), hoy)
        assertEquals(listOf(TipoAlerta.TAREA_ATRASADA), tipos(a))
        assertEquals(SeveridadAlerta.URGENTE, a.first().severidad)
    }

    @Test fun `estudiante no recibe alertas de tareas de otros`() {
        val a = generarAlertas(est3, listOf(tarea(fecha = hoy - dia)), emptyList(), listOf(sector), emptyList(), hoy)
        assertTrue(a.isEmpty())
    }

    @Test fun `riego atrasado cuando ya paso la fecha`() {
        val c = cultivo(siembra = hoy - 5 * dia, frecuencia = 2)
        val a = generarAlertas(admin, emptyList(), listOf(c), listOf(sector), emptyList(), hoy)
        assertEquals(listOf(TipoAlerta.RIEGO_ATRASADO), tipos(a))
    }

    @Test fun `riego proximo dentro de los dias de aviso`() {
        val c = cultivo(siembra = hoy - dia, frecuencia = 2)
        val a = generarAlertas(admin, emptyList(), listOf(c), listOf(sector), emptyList(), hoy)
        assertEquals(listOf(TipoAlerta.RIEGO_PROXIMO), tipos(a))
    }

    @Test fun `un riego validado reinicia la cuenta`() {
        val c = cultivo(siembra = hoy - 10 * dia, frecuencia = 3)
        val riego = tarea(tipo = TipoTarea.RIEGO, estado = EstadoTarea.VALIDADA, fecha = hoy, cultivoId = 1)
        val a = generarAlertas(admin, listOf(riego), listOf(c), listOf(sector), emptyList(), hoy)
        assertTrue(a.isEmpty())
    }

    @Test fun `cosecha proxima genera aviso informativo`() {
        val c = cultivo(siembra = hoy - 30 * dia, frecuencia = 60, cosecha = hoy + 3 * dia)
        val a = generarAlertas(admin, emptyList(), listOf(c), listOf(sector), emptyList(), hoy)
        assertEquals(listOf(TipoAlerta.COSECHA_PROXIMA), tipos(a))
        assertEquals(SeveridadAlerta.INFO, a.first().severidad)
    }

    @Test fun `cultivo cosechado no genera alertas`() {
        val c = cultivo(siembra = hoy - 30 * dia, cosecha = hoy, estado = EstadoCultivo.COSECHADO)
        assertTrue(generarAlertas(admin, emptyList(), listOf(c), listOf(sector), emptyList(), hoy).isEmpty())
    }

    @Test fun `mantenimiento programado se avisa`() {
        val m = tarea(tipo = TipoTarea.MANTENIMIENTO, fecha = hoy + 2 * dia)
        val a = generarAlertas(admin, listOf(m), emptyList(), listOf(sector), emptyList(), hoy)
        assertEquals(listOf(TipoAlerta.MANTENIMIENTO), tipos(a))
    }

    @Test fun `admin ve por validar y problemas pero el estudiante no`() {
        val t = tarea(estado = EstadoTarea.PENDIENTE_DE_VALIDACION)
        val p = problema()
        val deAdmin = generarAlertas(admin, listOf(t), emptyList(), listOf(sector), listOf(p), hoy)
        assertTrue(TipoAlerta.POR_VALIDAR in tipos(deAdmin))
        assertTrue(TipoAlerta.PROBLEMA_PENDIENTE in tipos(deAdmin))

        val deEstudiante = generarAlertas(est1, listOf(t), emptyList(), listOf(sector), listOf(p), hoy)
        assertTrue(deEstudiante.isEmpty())
    }

    @Test fun `las urgentes van primero`() {
        val atrasada = tarea(id = 1, fecha = hoy - dia)
        val mant = tarea(id = 2, tipo = TipoTarea.MANTENIMIENTO, fecha = hoy + dia)
        val a = generarAlertas(est1, listOf(mant, atrasada), emptyList(), listOf(sector), emptyList(), hoy)
        assertEquals(SeveridadAlerta.URGENTE, a.first().severidad)
    }

    @Test fun `resumen cuenta segun el perfil`() {
        val tareas = listOf(
            tarea(id = 1, fecha = hoy - dia),
            tarea(id = 2, estado = EstadoTarea.PENDIENTE_DE_VALIDACION),
            tarea(id = 3, estado = EstadoTarea.VALIDADA),
            tarea(id = 4, asignado = "EST-02")
        )
        val r = construirResumen(est1, tareas, listOf(problema()), 2, hoy)
        assertEquals(1, r.pendientes)
        assertEquals(1, r.atrasadas)
        assertEquals(1, r.porValidar)
        assertEquals(1, r.validadas)
        assertEquals(1, r.problemasAbiertos)

        assertEquals(2, construirResumen(admin, tareas, emptyList(), 0, hoy).pendientes)
    }

    @Test fun `historial ordena del mas reciente al mas antiguo`() {
        val t = tarea(estado = EstadoTarea.VALIDADA).copy(
            fechaCreacion = 100L, fechaRealizacion = 200L, realizadaPorId = "EST-01",
            validadorId = "ADM-01", fechaValidacion = 300L
        )
        val h = construirHistorial(admin, listOf(t), emptyList(), mapOf("EST-01" to "Estudiante Uno"))
        assertEquals(
            listOf(TipoEvento.TAREA_VALIDADA, TipoEvento.TAREA_ENVIADA, TipoEvento.TAREA_ASIGNADA),
            h.map { it.tipo }
        )
    }

    @Test fun `historial del estudiante no incluye tareas ajenas`() {
        val h = construirHistorial(est3, listOf(tarea()), listOf(problema()), emptyMap())
        assertTrue(h.isEmpty())
    }

    @Test fun `una tarea reenviada no muestra la revision anterior`() {
        val t = tarea(estado = EstadoTarea.PENDIENTE_DE_VALIDACION).copy(
            fechaRealizacion = 400L, realizadaPorId = "EST-01", fechaValidacion = null
        )
        val h = construirHistorial(admin, listOf(t), emptyList(), emptyMap())
        assertTrue(h.none { it.tipo == TipoEvento.TAREA_VALIDADA || it.tipo == TipoEvento.TAREA_RECHAZADA })
    }
}