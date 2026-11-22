package cl.duoc.huerto.testutil

import cl.duoc.huerto.core.session.SesionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Reemplaza Dispatchers.Main (que en pruebas unitarias no existe) por un dispatcher de prueba
 * y deja la sesión cerrada antes y después de cada prueba.
 * Con UnconfinedTestDispatcher las corrutinas del ViewModel se ejecutan al instante.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
        SesionManager.cerrar()
    }

    override fun finished(description: Description) {
        SesionManager.cerrar()
        Dispatchers.resetMain()
    }
}
