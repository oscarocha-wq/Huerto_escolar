package cl.duoc.huerto.u_i.tareas

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.duoc.huerto.data.AppDatabase
import cl.duoc.huerto.data.SesionManager
import cl.duoc.huerto.data.entities.Evidencia
import cl.duoc.huerto.data.entities.Tarea
import cl.duoc.huerto.data.entities.Usuario
import cl.duoc.huerto.data.entities.Validacion
import cl.duoc.huerto.data.repository.*
import cl.duoc.huerto.domain.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DetalleUi(
    val comentario: String = "",
    val fotoRuta: String? = null,
    val error: String? = null,
    val mensaje: String? = null,
    val procesando: Boolean = false,
    val eliminada: Boolean = false
)

class TareaDetalleViewModel(
    private val repo: TareaRepository,
    private val seguimientoRepo: SeguimientoRepository,
    sectorRepo: SectorRepository,
    cultivoRepo: CultivoRepository,
    usuarioRepo: UsuarioRepository,
    private val tareaId: Int
) : ViewModel() {

    private val _ui = MutableStateFlow(DetalleUi())
    val ui: StateFlow<DetalleUi> = _ui.asStateFlow()

    // Solo el administrador necesita los nombres de los estudiantes
    private val estudiantes: Flow<List<Usuario>> =
        if (SesionManager.sesion.value?.esAdministrador == true) usuarioRepo.estudiantes()
        else flowOf(emptyList<Usuario>())

    val item: StateFlow<TareaItem?> = combine(
        repo.observar(tareaId), sectorRepo.sectores(), cultivoRepo.cultivos(), estudiantes
    ) { tarea, sectores, cultivos, est ->
        tarea?.let { construirItems(listOf(it), sectores, cultivos, est).firstOrNull() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** Entregas y revisiones de esta tarea (tablas Evidencia y Validacion). */
    val seguimiento: StateFlow<Seguimiento> = combine(
        seguimientoRepo.evidenciasDe(tareaId), seguimientoRepo.validacionesDe(tareaId)
    ) { evidencias, validaciones -> Seguimiento(evidencias, validaciones) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Seguimiento())

    fun onComentario(v: String) = _ui.update { it.copy(comentario = v, error = null) }

    fun onFoto(ruta: String?) = _ui.update {
        if (ruta == null) it.copy(error = "No se pudo cargar la imagen")
        else it.copy(fotoRuta = ruta, error = null)
    }
    fun onErrorFoto(mensaje: String) = _ui.update { it.copy(error = mensaje) }
    fun quitarFoto() = _ui.update { it.copy(fotoRuta = null) }

    fun mensajeMostrado() = _ui.update { it.copy(mensaje = null) }

    private suspend fun tareaDeEstudiante(): Pair<Sesion, Tarea>? {
        val sesion = SesionManager.sesion.value
        val tarea = repo.porId(tareaId)
        if (sesion == null || tarea == null || sesion.perfil != Perfil.ESTUDIANTE ||
            !estaAsignadaA(sesion, tarea.asignadoId, tarea.grupo)
        ) {
            _ui.update { it.copy(error = "No tienes permiso para trabajar en esta tarea") }
            return null
        }
        return sesion to tarea
    }

    fun iniciar() {
        viewModelScope.launch {
            val (_, tarea) = tareaDeEstudiante() ?: return@launch
            if (!puedeIniciar(tarea.estado)) {
                _ui.update { it.copy(error = "Esta tarea ya fue iniciada") }
                return@launch
            }
            repo.actualizar(tarea.copy(estado = EstadoTarea.EN_DESARROLLO))
            _ui.update { it.copy(mensaje = "Tarea iniciada") }
        }
    }

    fun marcarRealizada() {
        viewModelScope.launch {
            val (sesion, tarea) = tareaDeEstudiante() ?: return@launch
            if (!puedeMarcarRealizada(tarea.estado)) {
                _ui.update { it.copy(error = "Esta tarea no se puede marcar como realizada") }
                return@launch
            }
            val u = _ui.value
            val comentario = u.comentario.trim()
            val error = validarEntrega(comentario, u.fotoRuta)
            if (error != null) {
                _ui.update { it.copy(error = error) }
                return@launch
            }
            _ui.update { it.copy(procesando = true) }
            // Se guarda una evidencia nueva y la tarea queda pendiente de validación
            // (el estudiante nunca la deja validada). Todo en una transacción.
            seguimientoRepo.entregar(
                Evidencia(
                    tareaId = tarea.id,
                    autorId = sesion.usuarioId,
                    fecha = System.currentTimeMillis(),
                    comentario = comentario.ifBlank { null },
                    fotoRuta = u.fotoRuta
                )
            )
            _ui.value = DetalleUi(mensaje = "Tarea enviada para validación")
        }
    }

    fun validar() = revisar(aprobar = true)
    fun rechazar() = revisar(aprobar = false)

    private fun revisar(aprobar: Boolean) {
        viewModelScope.launch {
            val sesion = SesionManager.sesion.value
            val tarea = repo.porId(tareaId) ?: return@launch
            val entrega = seguimientoRepo.ultimaEvidencia(tareaId)
            if (sesion == null || !puedeRevisar(sesion, tarea.estado, entrega?.autorId)) {
                _ui.update { it.copy(error = "No tienes permiso para revisar esta tarea") }
                return@launch
            }
            val comentario = _ui.value.comentario.trim()
            val errorComentario = if (aprobar) {
                Reglas.largo(comentario, "el comentario", LimitesCampo.COMENTARIO)
            } else {
                Reglas.requeridoYLargo(
                    comentario, "el motivo del rechazo",
                    LimitesCampo.COMENTARIO, min = LimitesCampo.MOTIVO_RECHAZO_MIN
                )
            }
            if (errorComentario != null) {
                _ui.update { it.copy(error = errorComentario) }
                return@launch
            }
            _ui.update { it.copy(procesando = true) }
            seguimientoRepo.registrarValidacion(
                Validacion(
                    tareaId = tarea.id,
                    evidenciaId = entrega?.id,
                    validadorId = sesion.usuarioId,
                    resultado = if (aprobar) ResultadoValidacion.APROBADA else ResultadoValidacion.RECHAZADA,
                    comentario = comentario.ifBlank { null },
                    fecha = System.currentTimeMillis()
                )
            )
            _ui.value = DetalleUi(
                mensaje = if (aprobar) "Tarea validada" else "Tarea rechazada: el estudiante deberá repetirla"
            )
        }
    }

    fun eliminar() {
        viewModelScope.launch {
            val sesion = SesionManager.sesion.value
            val tarea = repo.porId(tareaId) ?: return@launch
            val entregas = seguimientoRepo.contarEvidencias(tareaId)
            if (!puedeEliminarTarea(sesion, tarea.estado, entregas)) {
                _ui.update {
                    it.copy(error = "Solo se pueden eliminar tareas pendientes que nadie ha entregado")
                }
                return@launch
            }
            _ui.update { it.copy(procesando = true) }
            try {
                repo.eliminar(tarea)
                _ui.update { it.copy(procesando = false, eliminada = true) }
            } catch (e: Exception) {
                _ui.update { it.copy(procesando = false, error = "No se pudo eliminar la tarea") }
            }
        }
    }

    companion object {
        fun factory(app: Application, tareaId: Int) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val db = AppDatabase.get(app)
                return TareaDetalleViewModel(
                    TareaRepository(db.tareaDao()),
                    SeguimientoRepository(db.seguimientoDao()),
                    SectorRepository(db.sectorDao()),
                    CultivoRepository(db.cultivoDao()),
                    UsuarioRepository(db.usuarioDao()),
                    tareaId
                ) as T
            }
        }
    }
}
