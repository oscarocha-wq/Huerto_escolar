package cl.duoc.huerto.presentation.screens.sensores

import cl.duoc.huerto.core.session.SesionManager
import cl.duoc.huerto.domain.model.LecturaSensor
import cl.duoc.huerto.domain.model.OrigenLectura
import cl.duoc.huerto.testutil.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class SensoresViewModelTest {

    @get:Rule val main = MainDispatcherRule()

    private fun lectura(origen: OrigenLectura, sectorId: Int = 1) =
        LecturaSensor(sectorId = sectorId, fecha = 100L, humedadSuelo = 50.0, temperatura = 20.0, humedadAmbiente = 60.0, origen = origen)

    private val repo = FakeLecturaRepository(
        listOf(lectura(OrigenLectura.SIMULADA), lectura(OrigenLectura.SIMULADA), lectura(OrigenLectura.MANUAL), lectura(OrigenLectura.SIMULADA, sectorId = 2))
    )

    private fun crearVm() = SensoresViewModel(FakeSectorRepository(listOf(Datos.sector(1))), repo, sectorId = 1)

    @Test fun `muestra el sector y sus lecturas`() = runTest(main.dispatcher) {
        val vm = crearVm()
        backgroundScope.launch { vm.sector.collect {} }
        backgroundScope.launch { vm.lecturas.collect {} }

        assertEquals("S-01", vm.sector.value?.codigo)
        assertEquals(3, vm.lecturas.value?.size)
    }

    @Test fun `sin permiso no registra lecturas`() {
        SesionManager.iniciar(Datos.EST1)
        val vm = crearVm()

        vm.simular()
        vm.registrarManual()
        vm.borrarSimuladas()

        assertEquals("No tienes permiso para registrar lecturas", vm.form.value.error)
        assertTrue(repo.guardadas.isEmpty())
    }

    @Test fun `simular genera un dato de prueba`() {
        SesionManager.iniciar(Datos.ADMIN)
        val vm = crearVm()

        vm.simular()

        assertEquals(OrigenLectura.SIMULADA, repo.guardadas.single().origen)
        assertEquals("Lectura de prueba generada", vm.form.value.mensaje)
        vm.mensajeMostrado()
        assertNull(vm.form.value.mensaje)
    }

    @Test fun `registro manual con errores`() {
        SesionManager.iniciar(Datos.ADMIN)
        val vm = crearVm()
        vm.onHumedadSuelo("abc")
        vm.onTemperatura("")
        vm.onHumedadAmbiente("150")

        vm.registrarManual()

        val err = vm.form.value.errores
        assertNotNull(err.humedadSuelo)
        assertNotNull(err.temperatura)
        assertNotNull(err.humedadAmbiente)
        assertTrue(repo.guardadas.isEmpty())

        vm.onHumedadSuelo("40")
        assertNull(vm.form.value.errores.humedadSuelo)
    }

    @Test fun `registro manual valido`() {
        SesionManager.iniciar(Datos.ADMIN)
        val vm = crearVm()
        vm.onHumedadSuelo("45,5")
        vm.onTemperatura("21")
        vm.onHumedadAmbiente("70.2")

        vm.registrarManual()

        val l = repo.guardadas.single()
        assertEquals(45.5, l.humedadSuelo, 0.001)
        assertEquals(70.2, l.humedadAmbiente, 0.001)
        assertEquals(OrigenLectura.MANUAL, l.origen)
        assertEquals("Lectura registrada", vm.form.value.mensaje)
        assertEquals("", vm.form.value.humedadSuelo)
    }

    @Test fun `borrar datos de prueba del sector`() {
        SesionManager.iniciar(Datos.ADMIN)
        val vm = crearVm()

        vm.borrarSimuladas()

        assertEquals("Se borraron 2 dato(s) de prueba", vm.form.value.mensaje)
    }
}
