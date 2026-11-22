package cl.duoc.huerto.presentation.screens.tareas

import cl.duoc.huerto.core.session.SesionManager
import cl.duoc.huerto.domain.model.TipoTarea
import cl.duoc.huerto.testutil.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class TareaFormViewModelTest {

    @get:Rule val main = MainDispatcherRule()

    private val repo = FakeTareaRepository()

    private fun crearVm() = TareaFormViewModel(
        repo,
        FakeSectorRepository(listOf(Datos.sector(1))),
        FakeCultivoRepository(listOf(Datos.cultivo(1))),
        FakeUsuarioRepository(
            listOf(
                Datos.estudiante("EST-01", grupo = "Taller B"),
                Datos.estudiante("EST-02", grupo = "Taller A"),
                Datos.estudiante("EST-03", grupo = "Taller A"),
                Datos.estudiante("EST-04", grupo = null)
            )
        )
    )

    private fun llenarValido(vm: TareaFormViewModel) {
        vm.onTipo(TipoTarea.RIEGO)
        vm.onDescripcion("Regar las lechugas temprano")
        vm.onSector(Datos.sector(1))
        vm.onCultivo(Datos.cultivo(1))
        vm.onFecha(1_000L)
    }

    @Test fun `carga sectores cultivos estudiantes y grupos`() {
        val e = crearVm().estado.value
        assertEquals(1, e.sectores.size)
        assertEquals(1, e.cultivos.size)
        assertEquals(4, e.estudiantes.size)
        assertEquals(listOf("Taller A", "Taller B"), e.grupos)
    }

    @Test fun `cambiar de sector borra el cultivo elegido`() {
        val vm = crearVm()
        vm.onCultivo(Datos.cultivo(1))
        assertEquals(1, vm.estado.value.cultivoId)
        vm.onSector(Datos.sector(1))
        assertNull(vm.estado.value.cultivoId)
        vm.onCultivo(null)
        assertNull(vm.estado.value.cultivoId)
    }

    @Test fun `sin permiso no crea la tarea`() {
        val vm = crearVm()
        llenarValido(vm)
        vm.onEstudiante(Datos.estudiante("EST-01"))

        vm.guardar()

        assertEquals("No tienes permiso para crear tareas", vm.estado.value.errorGeneral)
        assertTrue(repo.creadas.isEmpty())
    }

    @Test fun `sin responsable muestra error de asignacion`() {
        SesionManager.iniciar(Datos.ADMIN)
        val vm = crearVm()
        llenarValido(vm)

        vm.guardar()

        assertNotNull(vm.estado.value.errores.asignado)
        assertTrue(repo.creadas.isEmpty())
    }

    @Test fun `formulario vacio muestra todos los errores`() {
        SesionManager.iniciar(Datos.ADMIN)
        val vm = crearVm()

        vm.guardar()

        val err = vm.estado.value.errores
        assertNotNull(err.tipo)
        assertNotNull(err.descripcion)
        assertNotNull(err.sector)
        assertNotNull(err.fecha)
    }

    @Test fun `crea una tarea asignada a un estudiante`() {
        SesionManager.iniciar(Datos.ADMIN)
        val vm = crearVm()
        llenarValido(vm)
        vm.onModo(ModoAsignacion.ESTUDIANTE)
        vm.onEstudiante(Datos.estudiante("EST-01"))

        vm.guardar()

        val t = repo.creadas.single()
        assertEquals("EST-01", t.asignadoId)
        assertNull(t.grupo)
        assertEquals("ADM-01", t.creadaPorId)
        assertEquals(1, t.cultivoId)
        assertTrue(vm.estado.value.guardado)
    }

    @Test fun `crea una tarea asignada a un grupo`() {
        SesionManager.iniciar(Datos.ADMIN)
        val vm = crearVm()
        llenarValido(vm)
        vm.onEstudiante(Datos.estudiante("EST-01"))
        vm.onModo(ModoAsignacion.GRUPO)
        vm.onGrupo("Taller A")

        vm.guardar()

        val t = repo.creadas.single()
        assertNull(t.asignadoId)
        assertEquals("Taller A", t.grupo)
    }
}
