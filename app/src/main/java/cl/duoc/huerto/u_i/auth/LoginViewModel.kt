package cl.duoc.huerto.u_i.auth

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.duoc.huerto.data.AppDatabase
import cl.duoc.huerto.data.SesionManager
import cl.duoc.huerto.data.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val usuario: String = "",
    val clave: String = "",
    val cargando: Boolean = false,
    val error: String? = null,
    val loginExitoso: Boolean = false
)

class LoginViewModel(private val repo: UsuarioRepository) : ViewModel() {

    private val _estado = MutableStateFlow(LoginUiState())
    val estado: StateFlow<LoginUiState> = _estado.asStateFlow()

    fun onUsuarioChange(valor: String) =
        _estado.update { it.copy(usuario = valor, error = null) }

    fun onClaveChange(valor: String) =
        _estado.update { it.copy(clave = valor, error = null) }

    fun iniciarSesion() {
        val actual = _estado.value
        if (actual.usuario.isBlank() || actual.clave.isBlank()) {
            _estado.update { it.copy(error = "Ingresa tu usuario y clave") }
            return
        }
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }
            val sesion = repo.login(actual.usuario, actual.clave)
            if (sesion == null) {
                _estado.update { it.copy(cargando = false, error = "Usuario o clave incorrectos") }
            } else {
                SesionManager.iniciar(sesion)
                _estado.update { it.copy(cargando = false, loginExitoso = true) }
            }
        }
    }

    fun loginConsumido() = _estado.update { it.copy(loginExitoso = false) }

    companion object {
        fun factory(app: Application) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val dao = AppDatabase.get(app).usuarioDao()
                return LoginViewModel(UsuarioRepository(dao)) as T
            }
        }
    }
}