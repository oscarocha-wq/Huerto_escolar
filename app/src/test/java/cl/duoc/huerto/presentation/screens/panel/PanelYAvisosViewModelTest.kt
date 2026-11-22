package cl.duoc.huerto.presentation.screens.panel

import cl.duoc.huerto.core.session.SesionManager
import cl.duoc.huerto.domain.model.EstadoProblema
import cl.duoc.huerto.domain.model.EstadoTarea
import cl.duoc.huerto.domain.model.ResumenAvisos
import cl.duoc.huerto.presentation.screens.avisos.AvisosViewModel
import cl.duoc.huerto.testutil.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/** Panel de inicio y resumen de avisos (notificación). */
class PanelYAvisosViewModelTest {

    @get:Rule val main = MainDispatcherRule()

    private val tareas = listOf(
        Datos.tarea(1, estado = EstadoTarea.PENDIENTE, asignadoId = "EST-01", fechaProgramada = Datos.hoy() - 3 * Datos.DIA),
        Datos.tarea(2, estado = EstadoTarea.PENDIENTE_DE_VALIDACION, asignadoId = "EST-02"),
        Datos.tarea(3, estado = EstadoTarea.VALIDADA, asignadoId = "EST-01")
    )
    private val problemas = listOf(
        Datos.problema(1, estado = EstadoProblema.INFORMADO, reportadoPorId = "EST-01"),
        Datos.problema(2, estado = EstadoProblema.CERRADO, reportadoPorId = "EST-02")
    )

    private fun panelVm() = PanelViewModel(
        FakeTareaRepository(tareas),
        FakeCultivoRepository(listOf(Datos.cultivo(1))),
        FakeSectorRepository(listOf(Datos.sector(1))),
        FakeProblemaRepository(problemas),
        FakeUsuarioRepository(listOf(Datos.estudiante("EST-01"), Datos.estudiante("EST-02"))),
        FakeSeguimientoRepository(listOf(Datos.evidencia(1, tareaId = 2, autorId = "EST-02")))
    )

    private fun avisosVm() = AvisosViewModel(FakeTareaRepository(tareas), FakeProblemaRepository(problemas))

    // ---------- Panel ----------

    @Test fun `sin sesion el panel queda vacio`() = runTest(main.dispatcher) {
        val vm = panelVm()
        backgroundScope.launch { vm.ui.collect {} }
        assertNull(vm.ui.value.sesion)
        assertTrue(vm.ui.value.alertas.isEmpty())
    }

    @Test fun `panel del administrador`() = runTest(main.dispatcher) {
        SesionManager.iniciar(Datos.ADMIN)
        val vm = panelVm()
        backgroundScope.launch { vm.ui.collect {} }

        val ui = vm.ui.value
        assertEquals("Docente Prueba", ui.nombre)
        assertTrue(ui.esAdmin)
        assertEquals(Datos.ADMIN, ui.sesion)
        assertEquals(ui.alertas.size, ui.resumen.alertas)
        assertTrue(ui.alertas.isNotEmpty())
    }

    @Test fun `panel del estudiante`() = runTest(main.dispatcher) {
        SesionManager.iniciar(Datos.EST1)
        val vm = panelVm()
        backgroundScope.launch { vm.ui.collect {} }

        val ui = vm.ui.value
        assertEquals("Estudiante Uno", ui.nombre)
        assertFalse(ui.esAdmin)
    }

    // ---------- Avisos ----------

    @Test fun `sin sesion no hay avisos`() = runTest(main.dispatcher) {
        val vm = avisosVm()
        backgroundScope.launch { vm.resumen.collect {} }
        assertEquals(ResumenAvisos(), vm.resumen.value)
    }

    @Test fun `avisos del administrador`() = runTest(main.dispatcher) {
        SesionManager.iniciar(Datos.ADMIN)
        val vm = avisosVm()
        backgroundScope.launch { vm.resumen.collect {} }

        assertEquals(ResumenAvisos(atrasadas = 1, porValidar = 1, problemasNuevos = 1), vm.resumen.value)
    }

    @Test fun `avisos del estudiante solo cuentan sus tareas`() = runTest(main.dispatcher) {
        SesionManager.iniciar(Datos.EST1)
        val vm = avisosVm()
        backgroundScope.launch { vm.resumen.collect {} }

        assertEquals(ResumenAvisos(atrasadas = 1, porValidar = 0, problemasNuevos = 0), vm.resumen.value)
    }
}
