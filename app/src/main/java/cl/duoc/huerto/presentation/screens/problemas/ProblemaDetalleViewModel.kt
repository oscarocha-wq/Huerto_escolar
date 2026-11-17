package cl.duoc.huerto.presentation.screens.problemas

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.duoc.huerto.core.di.AppContainer
import cl.duoc.huerto.core.session.SesionManager
import cl.duoc.huerto.domain.model.*
import cl.duoc.huerto.domain.repository.*
import cl.duoc.huerto.domain.rules.*
import cl.duoc.huerto.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProblemaDetalleViewModel(
    private val repo: ProblemaRepository,
    sectorRepo: SectorRepository,
    cultivoRepo: CultivoRepository,
    usuarioRepo: UsuarioRepository,
    private val problemaId: Int
) : ViewModel() {

    private val _ui = MutableStateFlow(DetalleProblemaUi())
    val ui: StateFlow<DetalleProblemaUi> = _ui.asStateFlow()

    private val usuarios: Flow<List<Usuario>> =
        if (SesionManager.sesion.value?.esAdministrador == true) usuarioRepo.estudiantes()
        else flowOf(emptyList<Usuario>())

    val item: StateFlow<ProblemaItem?> = combine(
        repo.observar(problemaId), sectorRepo.sectores(), cultivoRepo.cultivos(), usuarios
    ) { problema, sectores, cultivos, u ->
        problema?.let { construirItemsProblema(listOf(it), sectores, cultivos, u).firstOrNull() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun onComentario(v: String) = _ui.update { it.copy(comentario = v, error = null) }
    fun mensajeMostrado() = _ui.update { it.copy(mensaje = null) }

    fun cambiarEstado(nuevo: EstadoProblema) {
        viewModelScope.launch {
            val sesion = SesionManager.sesion.value
            val problema = repo.porId(problemaId) ?: return@launch

            if (!puedeGestionarProblema(sesion)) {
                _ui.update { it.copy(error = "Solo un docente o tallerista puede cambiar el estado") }
                return@launch
            }
            if (nuevo !in siguientesEstados(problema.estado)) {
                _ui.update { it.copy(error = "Ese cambio de estado no está permitido") }
                return@launch
            }
            val comentario = _ui.value.comentario.trim()
            if (requiereComentario(nuevo) && comentario.isBlank()) {
                _ui.update { it.copy(error = "Escribe un comentario sobre cómo se atendió el problema") }
                return@launch
            }

            _ui.update { it.copy(procesando = true) }
            val ahora = System.currentTimeMillis()
            repo.actualizar(
                problema.copy(
                    estado = nuevo,
                    comentarioDocente = comentario.ifBlank { problema.comentarioDocente },
                    atendidoPorId = sesion?.usuarioId,
                    fechaAtencion = if (nuevo == EstadoProblema.ATENDIDO || nuevo == EstadoProblema.CERRADO)
                        ahora else null
                )
            )
            _ui.value = DetalleProblemaUi(mensaje = "Estado actualizado: ${nuevo.etiqueta()}")
        }
    }

    companion object {
        fun factory(app: Application, problemaId: Int) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ProblemaDetalleViewModel(
                    AppContainer.problemaRepository(app),
                    AppContainer.sectorRepository(app),
                    AppContainer.cultivoRepository(app),
                    AppContainer.usuarioRepository(app),
                    problemaId
                ) as T
            }
        }
    }
}
