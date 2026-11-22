package cl.duoc.huerto.presentation.screens.sectores

import cl.duoc.huerto.core.session.SesionManager
import cl.duoc.huerto.domain.model.DependenciasSector
import cl.duoc.huerto.domain.model.EstadoSector
import cl.duoc.huerto.testutil.Datos
import cl.duoc.huerto.testutil.FakeSectorRepository
import cl.duoc.huerto.testutil.MainDispatcherRule
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class SectorFormViewModelTest {

    @get:Rule val main = MainDispatcherRule()

    private fun llenarValido(vm: SectorFormViewModel) {
        vm.onCodigo(" s-09 ")
        vm.onNombre("Sector Nuevo")
        vm.onUbicacion("Patio trasero")
        vm.onDescripcion("Camas elevadas")
        vm.onEstado(EstadoSector.DISPONIBLE)
    }

    @Test fun `formulario nuevo no es edicion`() {
        val vm = SectorFormViewModel(FakeSectorRepository(), NUEVO_SECTOR)
        assertFalse(vm.estado.value.esEdicion)
        assertFalse(vm.estado.value.cargando)
    }

    @Test fun `en edicion carga los datos del sector`() {
        val vm = SectorFormViewModel(FakeSectorRepository(listOf(Datos.sector(5, codigo = "S-05"))), 5)
        val e = vm.estado.value
        assertTrue(e.esEdicion)
        assertFalse(e.cargando)
        assertEquals("S-05", e.codigo)
        assertEquals("Patio central", e.ubicacion)
        assertEquals(EstadoSector.EN_USO, e.estado)
    }

    @Test fun `en edicion de un sector inexistente muestra error`() {
        val vm = SectorFormViewModel(FakeSectorRepository(), 99)
        assertEquals("No se encontró el sector", vm.estado.value.errorGeneral)
    }

    @Test fun `sin sesion o como estudiante no puede guardar`() {
        val repo = FakeSectorRepository()
        val vm = SectorFormViewModel(repo, NUEVO_SECTOR)
        llenarValido(vm)

        vm.guardar()
        assertEquals("No tienes permiso para administrar sectores", vm.estado.value.errorGeneral)

        SesionManager.iniciar(Datos.EST1)
        vm.guardar()
        assertTrue(repo.guardados.isEmpty())
    }

    @Test fun `con datos invalidos muestra errores por campo y se limpian al escribir`() {
        SesionManager.iniciar(Datos.ADMIN)
        val vm = SectorFormViewModel(FakeSectorRepository(), NUEVO_SECTOR)

        vm.guardar()

        val errores = vm.estado.value.errores
        assertNotNull(errores.codigo)
        assertNotNull(errores.nombre)
        assertNotNull(errores.ubicacion)

        vm.onCodigo("S-1")
        vm.onNombre("Norte")
        vm.onUbicacion("Patio")
        assertNull(vm.estado.value.errores.codigo)
        assertNull(vm.estado.value.errores.nombre)
        assertNull(vm.estado.value.errores.ubicacion)
    }

    @Test fun `codigo duplicado no se guarda`() {
        SesionManager.iniciar(Datos.ADMIN)
        val repo = FakeSectorRepository().apply { duplicado = true }
        val vm = SectorFormViewModel(repo, NUEVO_SECTOR)
        llenarValido(vm)

        vm.guardar()

        assertEquals("Ya existe un sector con ese código", vm.estado.value.errores.codigo)
        assertTrue(repo.guardados.isEmpty())
    }

    @Test fun `guardar un sector nuevo normaliza el codigo`() {
        SesionManager.iniciar(Datos.ADMIN)
        val repo = FakeSectorRepository()
        val vm = SectorFormViewModel(repo, NUEVO_SECTOR)
        llenarValido(vm)

        vm.guardar()

        val s = repo.guardados.single()
        assertEquals(0, s.id)
        assertEquals("S-09", s.codigo)
        assertEquals("ADM-01", s.responsableId)
        assertTrue(vm.estado.value.guardado)
    }

    @Test fun `editar conserva id responsable y fecha de creacion`() {
        SesionManager.iniciar(Datos.ADMIN)
        val repo = FakeSectorRepository(listOf(Datos.sector(5, codigo = "S-05")))
        val vm = SectorFormViewModel(repo, 5)
        vm.onNombre("Renombrado")

        vm.guardar()

        val s = repo.guardados.single()
        assertEquals(5, s.id)
        assertEquals("Renombrado", s.nombre)
        assertEquals(100L, s.fechaCreacion)
    }

    @Test fun `eliminar sin permiso o sin sector cargado no hace nada`() {
        val repo = FakeSectorRepository()
        val vm = SectorFormViewModel(repo, NUEVO_SECTOR)

        vm.eliminar()
        assertEquals("No tienes permiso para eliminar sectores", vm.estado.value.errorGeneral)

        SesionManager.iniciar(Datos.ADMIN)
        vm.eliminar()
        assertTrue(repo.eliminados.isEmpty())
    }

    @Test fun `no elimina un sector con datos asociados`() {
        SesionManager.iniciar(Datos.ADMIN)
        val repo = FakeSectorRepository(listOf(Datos.sector(5))).apply {
            dependencias = DependenciasSector(cultivos = 2, tareas = 1)
        }
        val vm = SectorFormViewModel(repo, 5)

        vm.eliminar()

        assertTrue(vm.estado.value.errorGeneral!!.startsWith("No se puede eliminar"))
        assertTrue(repo.eliminados.isEmpty())
    }

    @Test fun `elimina un sector vacio`() {
        SesionManager.iniciar(Datos.ADMIN)
        val repo = FakeSectorRepository(listOf(Datos.sector(5)))
        val vm = SectorFormViewModel(repo, 5)

        vm.eliminar()

        assertTrue(vm.estado.value.eliminado)
        assertEquals(5, repo.eliminados.single().id)
    }

    @Test fun `si la base de datos rechaza el borrado muestra error`() {
        SesionManager.iniciar(Datos.ADMIN)
        val repo = FakeSectorRepository(listOf(Datos.sector(5))).apply { fallaAlEliminar = true }
        val vm = SectorFormViewModel(repo, 5)

        vm.eliminar()

        assertFalse(vm.estado.value.eliminado)
        assertEquals("No se pudo eliminar: el sector tiene datos asociados", vm.estado.value.errorGeneral)
    }
}
