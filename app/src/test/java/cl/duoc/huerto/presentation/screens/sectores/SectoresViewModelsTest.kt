package cl.duoc.huerto.presentation.screens.sectores

import cl.duoc.huerto.domain.model.EstadoProblema
import cl.duoc.huerto.domain.model.EstadoTarea
import cl.duoc.huerto.testutil.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/** Lista de sectores y detalle de un sector. */
class SectoresViewModelsTest {

    @get:Rule val main = MainDispatcherRule()

    @Test fun `la lista parte en null y luego muestra los sectores`() = runTest(main.dispatcher) {
        val vm = SectoresViewModel(FakeSectorRepository(listOf(Datos.sector(1), Datos.sector(2))))
        assertNull(vm.sectores.value)

        backgroundScope.launch { vm.sectores.collect {} }

        assertEquals(2, vm.sectores.value?.size)
    }

    @Test fun `el detalle cuenta tareas y problemas abiertos del sector`() = runTest(main.dispatcher) {
        val vm = SectorDetalleViewModel(
            FakeSectorRepository(listOf(Datos.sector(1), Datos.sector(2))),
            FakeCultivoRepository(
                listOf(
                    Datos.cultivo(1, sectorId = 1, nombre = "Zanahoria"),
                    Datos.cultivo(2, sectorId = 1, nombre = "Acelga"),
                    Datos.cultivo(3, sectorId = 2, nombre = "Tomate")
                )
            ),
            FakeTareaRepository(
                listOf(
                    Datos.tarea(1, sectorId = 1, estado = EstadoTarea.PENDIENTE),
                    Datos.tarea(2, sectorId = 1, estado = EstadoTarea.VALIDADA),
                    Datos.tarea(3, sectorId = 2)
                )
            ),
            FakeProblemaRepository(
                listOf(
                    Datos.problema(1, sectorId = 1, estado = EstadoProblema.INFORMADO),
                    Datos.problema(2, sectorId = 1, estado = EstadoProblema.CERRADO)
                )
            ),
            sectorId = 1
        )
        assertTrue(vm.ui.value.cargando)

        backgroundScope.launch { vm.ui.collect {} }

        val ui = vm.ui.value
        assertFalse(ui.cargando)
        assertEquals("S-01", ui.sector?.codigo)
        assertEquals(listOf("Acelga", "Zanahoria"), ui.cultivos.map { it.nombre })
        assertEquals(1, ui.tareasAbiertas)
        assertEquals(1, ui.problemasAbiertos)
    }
}
