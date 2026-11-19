package cl.duoc.huerto.presentation.screens.sincronizacion

import cl.duoc.huerto.domain.model.ResultadoSincronizacion
import cl.duoc.huerto.domain.repository.SincronizacionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException

private class FakeSincronizacionRepository(
    private val respuesta: Result<ResultadoSincronizacion>
) : SincronizacionRepository {
    var llamadas = 0
    override suspend fun sincronizar(): Result<ResultadoSincronizacion> {
        llamadas++
        return respuesta
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class SincronizacionViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test fun `estado inicial sin mensaje ni carga`() {
        val vm = SincronizacionViewModel(FakeSincronizacionRepository(Result.success(ResultadoSincronizacion(0, 0, 0, 0, 0))))
        assertFalse(vm.ui.value.cargando)
        assertNull(vm.ui.value.mensaje)
    }

    @Test fun `sincronizacion exitosa muestra el resumen`() = runTest(dispatcher) {
        val repo = FakeSincronizacionRepository(Result.success(ResultadoSincronizacion(4, 4, 5, 8, 3)))
        val vm = SincronizacionViewModel(repo)

        vm.sincronizar()
        assertTrue(vm.ui.value.cargando)
        advanceUntilIdle()

        assertFalse(vm.ui.value.cargando)
        assertFalse(vm.ui.value.esError)
        assertEquals("Listo: 4 sectores, 5 cultivos, 8 tareas y 3 problemas.", vm.ui.value.mensaje)
        assertEquals(1, repo.llamadas)
    }

    @Test fun `sin servidor muestra error`() = runTest(dispatcher) {
        val vm = SincronizacionViewModel(FakeSincronizacionRepository(Result.failure(IOException())))

        vm.sincronizar()
        advanceUntilIdle()

        assertTrue(vm.ui.value.esError)
        assertTrue(vm.ui.value.mensaje!!.startsWith("No se pudo conectar"))
    }

    @Test fun `no sincroniza dos veces mientras carga`() = runTest(dispatcher) {
        val repo = FakeSincronizacionRepository(Result.success(ResultadoSincronizacion(0, 0, 0, 0, 0)))
        val vm = SincronizacionViewModel(repo)

        vm.sincronizar()
        vm.sincronizar()
        advanceUntilIdle()

        assertEquals(1, repo.llamadas)
    }
}
