package cl.duoc.huerto.presentation.screens.tareas

import cl.duoc.huerto.core.session.SesionManager
import cl.duoc.huerto.domain.model.EstadoTarea
import cl.duoc.huerto.domain.model.ResultadoValidacion
import cl.duoc.huerto.domain.model.Tarea
import cl.duoc.huerto.testutil.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/** Flujo completo de una tarea: iniciar, entregar, validar, rechazar y eliminar. */
class TareaDetalleViewModelTest {

    @get:Rule val main = MainDispatcherRule()

    private lateinit var tareaRepo: FakeTareaRepository
    private lateinit var seguimiento: FakeSeguimientoRepository

    private fun crearVm(tarea: Tarea?, entregas: Int = 0): TareaDetalleViewModel {
        tareaRepo = FakeTareaRepository(listOfNotNull(tarea))
        seguimiento = FakeSeguimientoRepository(
            (1..entregas).map { Datos.evidencia(id = it, tareaId = 1, autorId = "EST-01", fecha = 100L * it) }
        )
        return TareaDetalleViewModel(
            tareaRepo, seguimiento,
            FakeSectorRepository(listOf(Datos.sector(1))),
            FakeCultivoRepository(),
            FakeUsuarioRepository(listOf(Datos.estudiante("EST-01", nombre = "Ana"))),
            tareaId = 1
        )
    }

    // ---------- Datos que muestra la pantalla ----------

    @Test fun `el administrador ve la tarea con el nombre del estudiante y su seguimiento`() = runTest(main.dispatcher) {
        SesionManager.iniciar(Datos.ADMIN)
        val vm = crearVm(Datos.tarea(1), entregas = 2)
        backgroundScope.launch { vm.item.collect {} }
        backgroundScope.launch { vm.seguimiento.collect {} }

        assertEquals("Ana", vm.item.value?.asignadoTexto)
        assertEquals(2, vm.seguimiento.value.intentos)
    }

    @Test fun `el estudiante ve el codigo en vez del nombre`() = runTest(main.dispatcher) {
        SesionManager.iniciar(Datos.EST1)
        val vm = crearVm(Datos.tarea(1))
        backgroundScope.launch { vm.item.collect {} }

        assertEquals("EST-01", vm.item.value?.asignadoTexto)
    }

    @Test fun `campos de la entrega`() {
        val vm = crearVm(Datos.tarea(1))
        vm.onFoto(null)
        assertEquals("No se pudo cargar la imagen", vm.ui.value.error)
        vm.onFoto("foto.jpg")
        assertEquals("foto.jpg", vm.ui.value.fotoRuta)
        assertNull(vm.ui.value.error)
        vm.onErrorFoto("Sin cámara")
        assertEquals("Sin cámara", vm.ui.value.error)
        vm.quitarFoto()
        assertNull(vm.ui.value.fotoRuta)
        vm.onComentario("Hecho")
        assertEquals("Hecho", vm.ui.value.comentario)
        assertNull(vm.ui.value.error)
        vm.mensajeMostrado()
        assertNull(vm.ui.value.mensaje)
    }

    // ---------- Estudiante ----------

    @Test fun `el estudiante asignado inicia la tarea`() {
        SesionManager.iniciar(Datos.EST1)
        val vm = crearVm(Datos.tarea(1, estado = EstadoTarea.PENDIENTE))

        vm.iniciar()

        assertEquals(EstadoTarea.EN_DESARROLLO, tareaRepo.actualizadas.single().estado)
        assertEquals("Tarea iniciada", vm.ui.value.mensaje)
    }

    @Test fun `no se inicia dos veces`() {
        SesionManager.iniciar(Datos.EST1)
        val vm = crearVm(Datos.tarea(1, estado = EstadoTarea.EN_DESARROLLO))

        vm.iniciar()

        assertEquals("Esta tarea ya fue iniciada", vm.ui.value.error)
    }

    @Test fun `quien no esta asignado no puede trabajar la tarea`() {
        SesionManager.iniciar(Datos.EST2)
        val vm = crearVm(Datos.tarea(1, asignadoId = "EST-01"))

        vm.iniciar()

        assertEquals("No tienes permiso para trabajar en esta tarea", vm.ui.value.error)
        assertTrue(tareaRepo.actualizadas.isEmpty())
    }

    @Test fun `el administrador tampoco marca tareas como realizadas`() {
        SesionManager.iniciar(Datos.ADMIN)
        val vm = crearVm(Datos.tarea(1))

        vm.marcarRealizada()

        assertEquals("No tienes permiso para trabajar en esta tarea", vm.ui.value.error)
    }

    @Test fun `sin tarea muestra error de permiso`() {
        SesionManager.iniciar(Datos.EST1)
        val vm = crearVm(null)

        vm.iniciar()

        assertEquals("No tienes permiso para trabajar en esta tarea", vm.ui.value.error)
    }

    @Test fun `entregar sin foto ni comentario muestra error`() {
        SesionManager.iniciar(Datos.EST1)
        val vm = crearVm(Datos.tarea(1, estado = EstadoTarea.EN_DESARROLLO))

        vm.marcarRealizada()

        assertEquals("Agrega una foto o un comentario como evidencia", vm.ui.value.error)
        assertTrue(seguimiento.entregadas.isEmpty())
    }

    @Test fun `entregar con comentario envia a validacion`() {
        SesionManager.iniciar(Datos.EST1)
        val vm = crearVm(Datos.tarea(1, estado = EstadoTarea.EN_DESARROLLO))
        vm.onComentario("  Regué todo  ")

        vm.marcarRealizada()

        val ev = seguimiento.entregadas.single()
        assertEquals("Regué todo", ev.comentario)
        assertEquals("EST-01", ev.autorId)
        assertEquals("Tarea enviada para validación", vm.ui.value.mensaje)
    }

    @Test fun `entregar solo con foto deja el comentario vacio`() {
        SesionManager.iniciar(Datos.EST1)
        val vm = crearVm(Datos.tarea(1, estado = EstadoTarea.RECHAZADA))
        vm.onFoto("foto.jpg")

        vm.marcarRealizada()

        assertNull(seguimiento.entregadas.single().comentario)
        assertEquals("foto.jpg", seguimiento.entregadas.single().fotoRuta)
    }

    @Test fun `una tarea validada no se vuelve a entregar`() {
        SesionManager.iniciar(Datos.EST1)
        val vm = crearVm(Datos.tarea(1, estado = EstadoTarea.VALIDADA))

        vm.marcarRealizada()

        assertEquals("Esta tarea no se puede marcar como realizada", vm.ui.value.error)
    }

    // ---------- Administrador: revisar ----------

    @Test fun `el administrador valida una entrega`() {
        SesionManager.iniciar(Datos.ADMIN)
        val vm = crearVm(Datos.tarea(1, estado = EstadoTarea.PENDIENTE_DE_VALIDACION), entregas = 1)

        vm.validar()

        val v = seguimiento.registradas.single()
        assertEquals(ResultadoValidacion.APROBADA, v.resultado)
        assertEquals(1, v.evidenciaId)
        assertNull(v.comentario)
        assertEquals("Tarea validada", vm.ui.value.mensaje)
    }

    @Test fun `rechazar exige un motivo`() {
        SesionManager.iniciar(Datos.ADMIN)
        val vm = crearVm(Datos.tarea(1, estado = EstadoTarea.PENDIENTE_DE_VALIDACION), entregas = 1)

        vm.rechazar()
        assertNotNull(vm.ui.value.error)
        assertTrue(seguimiento.registradas.isEmpty())

        vm.onComentario("Faltó regar el sector sur")
        vm.rechazar()

        assertEquals(ResultadoValidacion.RECHAZADA, seguimiento.registradas.single().resultado)
        assertEquals("Tarea rechazada: el estudiante deberá repetirla", vm.ui.value.mensaje)
    }

    @Test fun `un comentario demasiado largo al validar muestra error`() {
        SesionManager.iniciar(Datos.ADMIN)
        val vm = crearVm(Datos.tarea(1, estado = EstadoTarea.PENDIENTE_DE_VALIDACION), entregas = 1)
        vm.onComentario("x".repeat(400))

        vm.validar()

        assertNotNull(vm.ui.value.error)
        assertTrue(seguimiento.registradas.isEmpty())
    }

    @Test fun `un estudiante no puede revisar`() {
        SesionManager.iniciar(Datos.EST1)
        val vm = crearVm(Datos.tarea(1, estado = EstadoTarea.PENDIENTE_DE_VALIDACION), entregas = 1)

        vm.validar()

        assertEquals("No tienes permiso para revisar esta tarea", vm.ui.value.error)
    }

    @Test fun `revisar una tarea inexistente no hace nada`() {
        SesionManager.iniciar(Datos.ADMIN)
        val vm = crearVm(null)

        vm.validar()
        vm.eliminar()

        assertNull(vm.ui.value.error)
    }

    // ---------- Administrador: eliminar ----------

    @Test fun `elimina una tarea pendiente sin entregas`() {
        SesionManager.iniciar(Datos.ADMIN)
        val vm = crearVm(Datos.tarea(1))

        vm.eliminar()

        assertTrue(vm.ui.value.eliminada)
        assertEquals(1, tareaRepo.eliminadas.single().id)
    }

    @Test fun `no elimina una tarea con entregas`() {
        SesionManager.iniciar(Datos.ADMIN)
        val vm = crearVm(Datos.tarea(1), entregas = 1)

        vm.eliminar()

        assertEquals("Solo se pueden eliminar tareas pendientes que nadie ha entregado", vm.ui.value.error)
    }

    @Test fun `si falla el borrado muestra error`() {
        SesionManager.iniciar(Datos.ADMIN)
        val vm = crearVm(Datos.tarea(1))
        tareaRepo.fallaAlEliminar = true

        vm.eliminar()

        assertEquals("No se pudo eliminar la tarea", vm.ui.value.error)
        assertFalse(vm.ui.value.eliminada)
    }
}
