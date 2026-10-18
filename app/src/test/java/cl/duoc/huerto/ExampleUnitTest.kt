package cl.duoc.huerto

import cl.duoc.huerto.data.SesionManager
import cl.duoc.huerto.data.dao.UsuarioDao
import cl.duoc.huerto.data.entities.Usuario
import cl.duoc.huerto.data.repository.UsuarioRepository
import cl.duoc.huerto.domain.Perfil
import cl.duoc.huerto.u_i.auth.LoginViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

private class FakeUsuarioDao : UsuarioDao {
    private val usuarios = listOf(
        Usuario("ADM-01", "Docente Prueba", "1234", Perfil.ADMINISTRADOR, null),
        Usuario("EST-01", "Estudiante Uno", "1234", Perfil.ESTUDIANTE, "Taller A")
    )
    override suspend fun login(id: String, clave: String) =
        usuarios.firstOrNull { it.id == id && it.clave == clave }
    override fun estudiantes(): Flow<List<Usuario>> = emptyFlow()
    override suspend fun insertarTodos(usuarios: List<Usuario>) {}
}

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
        SesionManager.cerrar()
    }

    @After fun tearDown() { Dispatchers.resetMain() }

    private fun crearVm() = LoginViewModel(UsuarioRepository(FakeUsuarioDao()))

    @Test fun `login correcto de administrador inicia sesion`() = runTest(dispatcher) {
        val vm = crearVm()
        vm.onUsuarioChange("adm-01")
        vm.onClaveChange("1234")
        vm.iniciarSesion()
        advanceUntilIdle()

        assertTrue(vm.estado.value.loginExitoso)
        assertEquals(Perfil.ADMINISTRADOR, SesionManager.sesion.value?.perfil)
    }

    @Test fun `login con clave incorrecta muestra error`() = runTest(dispatcher) {
        val vm = crearVm()
        vm.onUsuarioChange("EST-01")
        vm.onClaveChange("0000")
        vm.iniciarSesion()
        advanceUntilIdle()

        assertFalse(vm.estado.value.loginExitoso)
        assertEquals("Usuario o clave incorrectos", vm.estado.value.error)
        assertNull(SesionManager.sesion.value)
    }

    @Test fun `campos vacios muestran error sin consultar`() = runTest(dispatcher) {
        val vm = crearVm()
        vm.iniciarSesion()
        advanceUntilIdle()

        assertEquals("Ingresa tu usuario y clave", vm.estado.value.error)
    }
}