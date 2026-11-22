package cl.duoc.huerto.presentation.screens.tareas

import cl.duoc.huerto.core.session.SesionManager
import cl.duoc.huerto.domain.model.EstadoTarea
import cl.duoc.huerto.testutil.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/** Lista de tareas, filtros y armado de los ítems. */
class TareasViewModelTest {

    @get:Rule val main = MainDispatcherRule()

    private val tareas = listOf(
        Datos.tarea(1, estado = EstadoTarea.PENDIENTE, asignadoId = "EST-01"),
        Datos.tarea(2, estado = EstadoTarea.PENDIENTE_DE_VALIDACION, asignadoId = "EST-02"),
        Datos.tarea(3, estado = EstadoTarea.VALIDADA, asignadoId = null, grupo = "Taller A"),
        Datos.tarea(4, estado = EstadoTarea.EN_DESARROLLO, asignadoId = "EST-09", fechaProgramada = Datos.hoy() - 2 * Datos.DIA)
    )

    private fun crearVm() = TareasViewModel(
        FakeTareaRepository(tareas),
        FakeSectorRepository(listOf(Datos.sector(1))),
        FakeCultivoRepository(),
        FakeUsuarioRepository(listOf(Datos.estudiante("EST-01"), Datos.estudiante("EST-02", nombre = "Estudiante Dos")))
    )

    @Test fun `sin sesion la lista queda vacia`() = runTest(main.dispatcher) {
        val vm = crearVm()
        backgroundScope.launch { vm.tareas.collect {} }
        assertEquals(emptyList<TareaItem>(), vm.tareas.value)
    }

    @Test fun `el administrador ve todas y puede filtrar`() = runTest(main.dispatcher) {
        SesionManager.iniciar(Datos.ADMIN)
        val vm = crearVm()
        backgroundScope.launch { vm.tareas.collect {} }

        assertEquals(4, vm.tareas.value?.size)

        vm.onFiltro(FiltroTarea.PENDIENTES)
        assertEquals(FiltroTarea.PENDIENTES, vm.filtro.value)
        assertEquals(listOf(1, 4), vm.tareas.value?.map { it.tarea.id })

        vm.onFiltro(FiltroTarea.POR_VALIDAR)
        assertEquals(listOf(2), vm.tareas.value?.map { it.tarea.id })

        vm.onFiltro(FiltroTarea.ATRASADAS)
        assertEquals(listOf(4), vm.tareas.value?.map { it.tarea.id })

        vm.onFiltro(FiltroTarea.VALIDADAS)
        assertEquals(listOf(3), vm.tareas.value?.map { it.tarea.id })
    }

    @Test fun `el estudiante solo ve sus tareas y las de su grupo`() = runTest(main.dispatcher) {
        SesionManager.iniciar(Datos.EST1)
        val vm = crearVm()
        backgroundScope.launch { vm.tareas.collect {} }

        assertEquals(listOf(1, 3), vm.tareas.value?.map { it.tarea.id })
    }

    @Test fun `construirItems arma los textos de cada tarea`() {
        val items = construirItems(
            tareas + Datos.tarea(5, sectorId = 99, asignadoId = null, grupo = null, cultivoId = 7),
            listOf(Datos.sector(1, nombre = "Norte")),
            listOf(Datos.cultivo(7, nombre = "Tomate")),
            listOf(Datos.estudiante("EST-01", nombre = "Ana"))
        )

        assertEquals("S-01 · Norte", items[0].sectorNombre)
        assertEquals("Ana", items[0].asignadoTexto)
        assertEquals("EST-02", items[1].asignadoTexto)          // sin nombre: muestra el código
        assertEquals("Grupo Taller A", items[2].asignadoTexto)
        assertTrue(items[3].atrasada)
        assertEquals("Sin asignar", items[4].asignadoTexto)
        assertEquals("Sector desconocido", items[4].sectorNombre)
        assertEquals("Tomate", items[4].cultivoNombre)
        assertNull(items[0].cultivoNombre)
    }

    @Test fun `cada filtro tiene titulo`() {
        FiltroTarea.entries.forEach { assertTrue(it.titulo.isNotBlank()) }
    }
}
