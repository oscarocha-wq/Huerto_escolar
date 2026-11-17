package cl.duoc.huerto.presentation.screens.cultivos

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.duoc.huerto.core.di.AppContainer
import cl.duoc.huerto.core.session.SesionManager
import cl.duoc.huerto.domain.model.*
import cl.duoc.huerto.domain.repository.CultivoRepository
import cl.duoc.huerto.domain.repository.SectorRepository
import cl.duoc.huerto.domain.rules.*
import cl.duoc.huerto.domain.usecase.*
import cl.duoc.huerto.domain.validation.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val NUEVO_CULTIVO = -1

class CultivoFormViewModel(
    private val repo: CultivoRepository,
    sectorRepo: SectorRepository,
    private val cultivoId: Int
) : ViewModel() {

    private val _estado = MutableStateFlow(
        CultivoFormState(
            esEdicion = cultivoId != NUEVO_CULTIVO,
            cargando = cultivoId != NUEVO_CULTIVO
        )
    )
    val estado: StateFlow<CultivoFormState> = _estado.asStateFlow()

    private var original: Cultivo? = null

    init {
        // Lista de sectores para el selector
        viewModelScope.launch {
            sectorRepo.sectores().collect { lista ->
                _estado.update { it.copy(sectores = lista) }
            }
        }
        if (cultivoId != NUEVO_CULTIVO) {
            viewModelScope.launch {
                val c = repo.porId(cultivoId)
                if (c == null) {
                    _estado.update { it.copy(cargando = false, errorGeneral = "No se encontró el cultivo") }
                } else {
                    original = c
                    _estado.update {
                        it.copy(
                            nombre = c.nombre,
                            descripcion = c.descripcion,
                            sectorId = c.sectorId,
                            fechaSiembra = c.fechaSiembra,
                            fechaTrasplante = c.fechaTrasplante,
                            fechaCosecha = c.fechaCosechaEstimada,
                            frecuencia = c.frecuenciaRiegoDias.toString(),
                            estado = c.estado,
                            observaciones = c.observaciones,
                            fotoRuta = c.fotoUri,
                            cargando = false
                        )
                    }
                }
            }
        }
    }

    // Al escribir en un campo, se limpia solo el error de ese campo
    fun onNombre(v: String) = _estado.update { it.copy(nombre = v, errores = it.errores.copy(nombre = null)) }
    fun onDescripcion(v: String) = _estado.update { it.copy(descripcion = v, errores = it.errores.copy(descripcion = null)) }
    fun onSector(s: Sector) = _estado.update { it.copy(sectorId = s.id, errores = it.errores.copy(sector = null)) }
    fun onFechaSiembra(v: Long?) = _estado.update { it.copy(fechaSiembra = v, errores = it.errores.copy(trasplante = null, cosecha = null)) }
    fun onFechaTrasplante(v: Long?) = _estado.update { it.copy(fechaTrasplante = v, errores = it.errores.copy(trasplante = null)) }
    fun onFechaCosecha(v: Long?) = _estado.update { it.copy(fechaCosecha = v, errores = it.errores.copy(cosecha = null)) }
    fun onFrecuencia(v: String) = _estado.update {
        it.copy(frecuencia = v.filter(Char::isDigit).take(2), errores = it.errores.copy(frecuencia = null))
    }
    fun onEstado(v: EstadoCultivo) = _estado.update { it.copy(estado = v) }
    fun onObservaciones(v: String) = _estado.update { it.copy(observaciones = v, errores = it.errores.copy(observaciones = null)) }

    fun onFoto(ruta: String) = _estado.update { it.copy(fotoRuta = ruta, errorGeneral = null) }
    fun onErrorFoto(mensaje: String) = _estado.update { it.copy(errorGeneral = mensaje) }
    fun quitarFoto() = _estado.update { it.copy(fotoRuta = null) }

    fun guardar() {
        // Control de acceso real
        val sesion = SesionManager.sesion.value
        if (!sesion.puedeAcceder(Perfil.ADMINISTRADOR)) {
            _estado.update { it.copy(errorGeneral = "No tienes permiso para administrar cultivos") }
            return
        }

        val a = _estado.value
        val errores = validarCultivo(
            a.nombre, a.sectorId, a.frecuencia, a.fechaSiembra, a.fechaTrasplante, a.fechaCosecha,
            a.descripcion, a.observaciones
        )
        if (errores.hayErrores) {
            _estado.update { it.copy(errores = errores, errorGeneral = null) }
            return
        }
        val sectorId = a.sectorId ?: return
        val frecuencia = a.frecuencia.toIntOrNull() ?: return

        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, errorGeneral = null) }
            val base = original
            repo.guardar(
                Cultivo(
                    id = base?.id ?: 0,
                    nombre = a.nombre.trim(),
                    descripcion = a.descripcion.trim(),
                    sectorId = sectorId,
                    fechaSiembra = a.fechaSiembra,
                    fechaTrasplante = a.fechaTrasplante,
                    fechaCosechaEstimada = a.fechaCosecha,
                    frecuenciaRiegoDias = frecuencia,
                    estado = a.estado,
                    observaciones = a.observaciones.trim(),
                    fotoUri = a.fotoRuta
                )
            )
            _estado.update { it.copy(cargando = false, guardado = true) }
        }
    }

    fun eliminar() {
        val sesion = SesionManager.sesion.value
        if (!sesion.puedeAcceder(Perfil.ADMINISTRADOR)) {
            _estado.update { it.copy(errorGeneral = "No tienes permiso para eliminar cultivos") }
            return
        }
        val cultivo = original ?: return
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, errorGeneral = null) }
            // Regla de negocio: no se borra un cultivo que tiene tareas
            val motivo = motivoNoEliminarCultivo(repo.contarTareas(cultivo.id))
            if (motivo != null) {
                _estado.update { it.copy(cargando = false, errorGeneral = motivo) }
                return@launch
            }
            try {
                repo.eliminar(cultivo)
                _estado.update { it.copy(cargando = false, eliminado = true) }
            } catch (e: Exception) {
                _estado.update { it.copy(cargando = false, errorGeneral = "No se pudo eliminar el cultivo") }
            }
        }
    }

    companion object {
        fun factory(app: Application, cultivoId: Int) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CultivoFormViewModel(
                    AppContainer.cultivoRepository(app),
                    AppContainer.sectorRepository(app),
                    cultivoId
                ) as T
            }
        }
    }
}
