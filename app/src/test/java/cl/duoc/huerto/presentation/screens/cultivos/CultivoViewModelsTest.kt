package cl.duoc.huerto.presentation.screens.cultivos

import cl.duoc.huerto.core.session.SesionManager
import cl.duoc.huerto.domain.model.EstadoCultivo
import cl.duoc.huerto.testutil.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class CultivoViewModelsTest {

    @get:Rule val main = MainDispatcherRule()

    // ---------- Lista ----------

    @Test fun `la lista ordena por nombre y muestra el sector`() = runTest(main.dispatcher) {
        val vm = CultivosViewModel(
            FakeCultivoRepository(
                listOf(Datos.cultivo(1, sectorId = 1, nombre = "Tomate"), Datos.cultivo(2, sectorId = 9, nombre = "Albahaca"))
            ),
            FakeSectorRepository(listOf(Datos.sector(1, nombre = "Norte")))
        )
        assertNull(vm.cultivos.value)

        backgroundScope.launch { vm.cultivos.collect {} }

        val items = vm.cultivos.value!!
        assertEquals(listOf("Albahaca", "Tomate"), items.map { it.cultivo.nombre })
        assertEquals("Sector desconocido", items[0].sectorNombre)
        assertEquals("S-01 · Norte", items[1].sectorNombre)
    }

    // ---------- Formulario ----------

    private fun llenarValido(vm: CultivoFormViewModel) {
        vm.onNombre("Lechuga")
        vm.onDescripcion("Escarola")
        vm.onSector(Datos.sector(1))
        vm.onFrecuencia("2")
        vm.onFechaSiembra(1_000L)
        vm.onFechaTrasplante(2_000L)
        vm.onFechaCosecha(5_000L)
        vm.onEstado(EstadoCultivo.SEMBRADO)
        vm.onObservaciones("Sin novedad")
    }

    @Test fun `carga la lista de sectores para el selector`() {
        val vm = CultivoFormViewModel(FakeCultivoRepository(), FakeSectorRepository(listOf(Datos.sector(1))), NUEVO_CULTIVO)
        assertEquals(1, vm.estado.value.sectores.size)
        assertFalse(vm.estado.value.esEdicion)
    }

    @Test fun `en edicion carga el cultivo`() {
        val vm = CultivoFormViewModel(FakeCultivoRepository(listOf(Datos.cultivo(3))), FakeSectorRepository(), 3)
        val e = vm.estado.value
        assertTrue(e.esEdicion)
        assertEquals("Lechuga", e.nombre)
        assertEquals("2", e.frecuencia)
        assertEquals("foto.jpg", e.fotoRuta)
        assertFalse(e.cargando)
    }

    @Test fun `en edicion de un cultivo inexistente muestra error`() {
        val vm = CultivoFormViewModel(FakeCultivoRepository(), FakeSectorRepository(), 99)
        assertEquals("No se encontró el cultivo", vm.estado.value.errorGeneral)
    }

    @Test fun `la frecuencia solo acepta hasta dos digitos`() {
        val vm = CultivoFormViewModel(FakeCultivoRepository(), FakeSectorRepository(), NUEVO_CULTIVO)
        vm.onFrecuencia("a1b2c3")
        assertEquals("12", vm.estado.value.frecuencia)
    }

    @Test fun `manejo de la foto`() {
        val vm = CultivoFormViewModel(FakeCultivoRepository(), FakeSectorRepository(), NUEVO_CULTIVO)
        vm.onErrorFoto("Sin permiso de cámara")
        assertEquals("Sin permiso de cámara", vm.estado.value.errorGeneral)
        vm.onFoto("ruta.jpg")
        assertEquals("ruta.jpg", vm.estado.value.fotoRuta)
        assertNull(vm.estado.value.errorGeneral)
        vm.quitarFoto()
        assertNull(vm.estado.value.fotoRuta)
    }

    @Test fun `sin permiso no guarda`() {
        val repo = FakeCultivoRepository()
        val vm = CultivoFormViewModel(repo, FakeSectorRepository(), NUEVO_CULTIVO)
        llenarValido(vm)

        vm.guardar()

        assertEquals("No tienes permiso para administrar cultivos", vm.estado.value.errorGeneral)
        assertTrue(repo.guardados.isEmpty())
    }

    @Test fun `con errores no guarda`() {
        SesionManager.iniciar(Datos.ADMIN)
        val repo = FakeCultivoRepository()
        val vm = CultivoFormViewModel(repo, FakeSectorRepository(), NUEVO_CULTIVO)
        vm.onFechaSiembra(5_000L)
        vm.onFechaCosecha(1_000L)

        vm.guardar()

        val err = vm.estado.value.errores
        assertNotNull(err.nombre)
        assertNotNull(err.sector)
        assertNotNull(err.frecuencia)
        assertNotNull(err.cosecha)
        assertTrue(repo.guardados.isEmpty())
    }

    @Test fun `guarda un cultivo nuevo`() {
        SesionManager.iniciar(Datos.ADMIN)
        val repo = FakeCultivoRepository()
        val vm = CultivoFormViewModel(repo, FakeSectorRepository(), NUEVO_CULTIVO)
        llenarValido(vm)

        vm.guardar()

        val c = repo.guardados.single()
        assertEquals(0, c.id)
        assertEquals(1, c.sectorId)
        assertEquals(2, c.frecuenciaRiegoDias)
        assertEquals(2_000L, c.fechaTrasplante)
        assertTrue(vm.estado.value.guardado)
    }

    @Test fun `editar conserva el id`() {
        SesionManager.iniciar(Datos.ADMIN)
        val repo = FakeCultivoRepository(listOf(Datos.cultivo(3)))
        val vm = CultivoFormViewModel(repo, FakeSectorRepository(), 3)
        vm.onNombre("Lechuga crespa")

        vm.guardar()

        assertEquals(3, repo.guardados.single().id)
        assertEquals("Lechuga crespa", repo.guardados.single().nombre)
    }

    @Test fun `eliminar sin permiso o sin cultivo cargado`() {
        val repo = FakeCultivoRepository()
        val vm = CultivoFormViewModel(repo, FakeSectorRepository(), NUEVO_CULTIVO)
        vm.eliminar()
        assertEquals("No tienes permiso para eliminar cultivos", vm.estado.value.errorGeneral)

        SesionManager.iniciar(Datos.ADMIN)
        vm.eliminar()
        assertTrue(repo.eliminados.isEmpty())
    }

    @Test fun `no elimina un cultivo con tareas`() {
        SesionManager.iniciar(Datos.ADMIN)
        val repo = FakeCultivoRepository(listOf(Datos.cultivo(3))).apply { tareas = 2 }
        val vm = CultivoFormViewModel(repo, FakeSectorRepository(), 3)

        vm.eliminar()

        assertTrue(vm.estado.value.errorGeneral!!.startsWith("No se puede eliminar"))
    }

    @Test fun `elimina un cultivo sin tareas`() {
        SesionManager.iniciar(Datos.ADMIN)
        val repo = FakeCultivoRepository(listOf(Datos.cultivo(3)))
        val vm = CultivoFormViewModel(repo, FakeSectorRepository(), 3)

        vm.eliminar()

        assertTrue(vm.estado.value.eliminado)
    }

    @Test fun `si falla el borrado muestra error`() {
        SesionManager.iniciar(Datos.ADMIN)
        val repo = FakeCultivoRepository(listOf(Datos.cultivo(3))).apply { fallaAlEliminar = true }
        val vm = CultivoFormViewModel(repo, FakeSectorRepository(), 3)

        vm.eliminar()

        assertEquals("No se pudo eliminar el cultivo", vm.estado.value.errorGeneral)
    }
}
