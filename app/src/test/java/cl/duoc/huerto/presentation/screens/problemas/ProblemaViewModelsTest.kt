package cl.duoc.huerto.presentation.screens.problemas

import cl.duoc.huerto.core.session.SesionManager
import cl.duoc.huerto.domain.model.EstadoProblema
import cl.duoc.huerto.domain.model.TipoProblema
import cl.duoc.huerto.domain.validation.MAX_DESCRIPCION_PROBLEMA
import cl.duoc.huerto.testutil.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/** Lista, reporte y atención de problemas. */
class ProblemaViewModelsTest {

    @get:Rule val main = MainDispatcherRule()

    private val problemas = listOf(
        Datos.problema(1, estado = EstadoProblema.INFORMADO, reportadoPorId = "EST-01"),
        Datos.problema(2, estado = EstadoProblema.ATENDIDO, reportadoPorId = "EST-02", cultivoId = 7),
        Datos.problema(3, sectorId = 99, estado = EstadoProblema.INFORMADO, reportadoPorId = "EST-09")
    )

    private fun listaVm() = ProblemasViewModel(
        FakeProblemaRepository(problemas),
        FakeSectorRepository(listOf(Datos.sector(1, nombre = "Norte"))),
        FakeCultivoRepository(listOf(Datos.cultivo(7, nombre = "Tomate"))),
        FakeUsuarioRepository(listOf(Datos.estudiante("EST-01", nombre = "Ana")))
    )

    // ---------- Lista ----------

    @Test fun `sin sesion no hay problemas`() = runTest(main.dispatcher) {
        val vm = listaVm()
        backgroundScope.launch { vm.problemas.collect {} }
        assertEquals(0, vm.problemas.value?.size)
    }

    @Test fun `el administrador ve todos y filtra por estado`() = runTest(main.dispatcher) {
        SesionManager.iniciar(Datos.ADMIN)
        val vm = listaVm()
        backgroundScope.launch { vm.problemas.collect {} }

        val items = vm.problemas.value!!
        assertEquals(3, items.size)
        assertEquals("Ana", items[0].reportadoPorTexto)
        assertEquals("S-01 · Norte", items[0].sectorNombre)
        assertEquals("Tomate", items[1].cultivoNombre)
        assertEquals("EST-09", items[2].reportadoPorTexto)
        assertEquals("Sector desconocido", items[2].sectorNombre)

        vm.onFiltro(EstadoProblema.INFORMADO)
        assertEquals(EstadoProblema.INFORMADO, vm.filtro.value)
        assertEquals(listOf(1, 3), vm.problemas.value?.map { it.problema.id })

        vm.onFiltro(null)
        assertEquals(3, vm.problemas.value?.size)
    }

    @Test fun `el estudiante solo ve lo que reporto`() = runTest(main.dispatcher) {
        SesionManager.iniciar(Datos.EST1)
        val vm = listaVm()
        backgroundScope.launch { vm.problemas.collect {} }

        assertEquals(listOf(1), vm.problemas.value?.map { it.problema.id })
        assertEquals("EST-01", vm.problemas.value?.first()?.reportadoPorTexto)
    }

    // ---------- Reportar ----------

    private val repoForm = FakeProblemaRepository()

    private fun formVm() = ProblemaFormViewModel(
        repoForm,
        FakeSectorRepository(listOf(Datos.sector(1))),
        FakeCultivoRepository(listOf(Datos.cultivo(1)))
    )

    @Test fun `el formulario carga sectores y cultivos`() {
        val e = formVm().estado.value
        assertEquals(1, e.sectores.size)
        assertEquals(1, e.cultivos.size)
    }

    @Test fun `campos del formulario`() {
        val vm = formVm()
        vm.onDescripcion("x".repeat(MAX_DESCRIPCION_PROBLEMA + 50))
        assertEquals(MAX_DESCRIPCION_PROBLEMA, vm.estado.value.descripcion.length)

        vm.onCultivo(Datos.cultivo(1))
        assertEquals(1, vm.estado.value.cultivoId)
        vm.onSector(Datos.sector(1))
        assertNull(vm.estado.value.cultivoId)
        vm.onCultivo(null)

        vm.onFoto(null)
        assertEquals("No se pudo cargar la imagen", vm.estado.value.errorGeneral)
        vm.onFoto("foto.jpg")
        assertEquals("foto.jpg", vm.estado.value.fotoRuta)
        vm.onErrorFoto("Sin permiso")
        assertEquals("Sin permiso", vm.estado.value.errorGeneral)
        vm.quitarFoto()
        assertNull(vm.estado.value.fotoRuta)
    }

    @Test fun `el administrador no reporta problemas`() {
        SesionManager.iniciar(Datos.ADMIN)
        val vm = formVm()

        vm.guardar()

        assertEquals("Solo los estudiantes pueden reportar problemas", vm.estado.value.errorGeneral)
    }

    @Test fun `con errores no se reporta`() {
        SesionManager.iniciar(Datos.EST1)
        val vm = formVm()
        vm.onDescripcion("corto")

        vm.guardar()

        val err = vm.estado.value.errores
        assertNotNull(err.tipo)
        assertNotNull(err.descripcion)
        assertNotNull(err.sector)
        assertTrue(repoForm.creados.isEmpty())
    }

    @Test fun `el estudiante reporta un problema`() {
        SesionManager.iniciar(Datos.EST1)
        val vm = formVm()
        vm.onTipo(TipoProblema.DANO_PLANTA)
        vm.onDescripcion("  Hojas mordidas en la cama 2  ")
        vm.onSector(Datos.sector(1))

        vm.guardar()

        val p = repoForm.creados.single()
        assertEquals("Hojas mordidas en la cama 2", p.descripcion)
        assertEquals("EST-01", p.reportadoPorId)
        assertEquals(EstadoProblema.INFORMADO, p.estado)
        assertTrue(vm.estado.value.guardado)
    }

    // ---------- Atender (detalle) ----------

    private lateinit var repoDetalle: FakeProblemaRepository

    private fun detalleVm(estado: EstadoProblema?): ProblemaDetalleViewModel {
        repoDetalle = FakeProblemaRepository(
            if (estado == null) emptyList() else listOf(Datos.problema(1, estado = estado))
        )
        return ProblemaDetalleViewModel(
            repoDetalle,
            FakeSectorRepository(listOf(Datos.sector(1))),
            FakeCultivoRepository(),
            FakeUsuarioRepository(listOf(Datos.estudiante("EST-01", nombre = "Ana"))),
            problemaId = 1
        )
    }

    @Test fun `el detalle muestra el problema`() = runTest(main.dispatcher) {
        SesionManager.iniciar(Datos.ADMIN)
        val vm = detalleVm(EstadoProblema.INFORMADO)
        backgroundScope.launch { vm.item.collect {} }

        assertEquals("Ana", vm.item.value?.reportadoPorTexto)
    }

    @Test fun `el estudiante ve el detalle sin nombres`() = runTest(main.dispatcher) {
        SesionManager.iniciar(Datos.EST1)
        val vm = detalleVm(EstadoProblema.INFORMADO)
        backgroundScope.launch { vm.item.collect {} }

        assertEquals("EST-01", vm.item.value?.reportadoPorTexto)
    }

    @Test fun `el administrador pasa el problema a revision`() {
        SesionManager.iniciar(Datos.ADMIN)
        val vm = detalleVm(EstadoProblema.INFORMADO)

        vm.cambiarEstado(EstadoProblema.EN_REVISION)

        val p = repoDetalle.actualizados.single()
        assertEquals(EstadoProblema.EN_REVISION, p.estado)
        assertEquals("ADM-01", p.atendidoPorId)
        assertNull(p.fechaAtencion)
        assertEquals("Estado actualizado: En revisión", vm.ui.value.mensaje)

        vm.mensajeMostrado()
        assertNull(vm.ui.value.mensaje)
    }

    @Test fun `marcar como atendido exige comentario`() {
        SesionManager.iniciar(Datos.ADMIN)
        val vm = detalleVm(EstadoProblema.EN_REVISION)

        vm.cambiarEstado(EstadoProblema.ATENDIDO)
        assertEquals("Escribe un comentario sobre cómo se atendió el problema", vm.ui.value.error)

        vm.onComentario("Se regó el sector")
        assertNull(vm.ui.value.error)
        vm.cambiarEstado(EstadoProblema.ATENDIDO)

        val p = repoDetalle.actualizados.single()
        assertEquals("Se regó el sector", p.comentarioDocente)
        assertNotNull(p.fechaAtencion)
    }

    @Test fun `no permite saltarse estados`() {
        SesionManager.iniciar(Datos.ADMIN)
        val vm = detalleVm(EstadoProblema.INFORMADO)

        vm.cambiarEstado(EstadoProblema.CERRADO)

        assertEquals("Ese cambio de estado no está permitido", vm.ui.value.error)
    }

    @Test fun `el estudiante no cambia estados`() {
        SesionManager.iniciar(Datos.EST1)
        val vm = detalleVm(EstadoProblema.INFORMADO)

        vm.cambiarEstado(EstadoProblema.EN_REVISION)

        assertEquals("Solo un docente o tallerista puede cambiar el estado", vm.ui.value.error)
    }

    @Test fun `problema inexistente no hace nada`() {
        SesionManager.iniciar(Datos.ADMIN)
        val vm = detalleVm(null)

        vm.cambiarEstado(EstadoProblema.EN_REVISION)

        assertNull(vm.ui.value.error)
        assertTrue(repoDetalle.actualizados.isEmpty())
    }
}
