package cl.duoc.huerto.presentation.screens.clima

import cl.duoc.huerto.domain.model.Clima
import cl.duoc.huerto.domain.repository.ClimaRepository
import cl.duoc.huerto.testutil.MainDispatcherRule
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.IOException

private class FakeClimaRepository(var respuesta: Result<Clima>) : ClimaRepository {
    var llamadas = 0
    override suspend fun climaActual(): Result<Clima> { llamadas++; return respuesta }
}

class ClimaViewModelTest {

    @get:Rule val main = MainDispatcherRule()

    private val clima = Clima(temperatura = 31.0, humedad = 40, lluviaHoyMm = 0.0, maxima = 33.0, minima = 15.0, codigo = 0)

    @Test fun `al abrir carga el clima con descripcion y consejo`() {
        val vm = ClimaViewModel(FakeClimaRepository(Result.success(clima)))

        val ui = vm.ui.value
        assertFalse(ui.cargando)
        assertEquals(clima, ui.clima)
        assertEquals("Despejado", ui.descripcion)
        assertEquals("Hace mucho calor: rieguen temprano o al atardecer.", ui.consejo)
        assertNull(ui.error)
    }

    @Test fun `sin internet muestra error y se puede reintentar`() {
        val repo = FakeClimaRepository(Result.failure(IOException()))
        val vm = ClimaViewModel(repo)
        assertNotNull(vm.ui.value.error)
        assertNull(vm.ui.value.clima)

        repo.respuesta = Result.success(clima)
        vm.cargar()

        assertEquals(2, repo.llamadas)
        assertNull(vm.ui.value.error)
        assertEquals(clima, vm.ui.value.clima)
    }
}
