package cl.duoc.huerto.presentation.screens.sectores

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.duoc.huerto.core.di.AppContainer
import cl.duoc.huerto.core.session.SesionManager
import cl.duoc.huerto.domain.model.EstadoSector
import cl.duoc.huerto.domain.model.Perfil
import cl.duoc.huerto.domain.model.Sector
import cl.duoc.huerto.domain.repository.SectorRepository
import cl.duoc.huerto.domain.rules.motivoNoEliminarSector
import cl.duoc.huerto.domain.rules.puedeAcceder
import cl.duoc.huerto.domain.validation.validarSector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val NUEVO_SECTOR = -1

class SectorFormViewModel(
    private val repo: SectorRepository,
    private val sectorId: Int
) : ViewModel() {

    private val _estado = MutableStateFlow(
        SectorFormState(
            esEdicion = sectorId != NUEVO_SECTOR,
            cargando = sectorId != NUEVO_SECTOR
        )
    )
    val estado: StateFlow<SectorFormState> = _estado.asStateFlow()

    private var original: Sector? = null

    init {
        if (sectorId != NUEVO_SECTOR) {
            viewModelScope.launch {
                val sector = repo.porId(sectorId)
                if (sector == null) {
                    _estado.update { it.copy(cargando = false, errorGeneral = "No se encontró el sector") }
                } else {
                    original = sector
                    _estado.update {
                        it.copy(
                            codigo = sector.codigo,
                            nombre = sector.nombre,
                            descripcion = sector.descripcion,
                            ubicacion = sector.ubicacion,
                            estado = sector.estado,
                            cargando = false
                        )
                    }
                }
            }
        }
    }

    // Al escribir en un campo, se limpia solo el error de ese campo
    fun onCodigo(v: String) = _estado.update { it.copy(codigo = v, errores = it.errores.copy(codigo = null)) }
    fun onNombre(v: String) = _estado.update { it.copy(nombre = v, errores = it.errores.copy(nombre = null)) }
    fun onDescripcion(v: String) = _estado.update { it.copy(descripcion = v, errores = it.errores.copy(descripcion = null)) }
    fun onUbicacion(v: String) = _estado.update { it.copy(ubicacion = v, errores = it.errores.copy(ubicacion = null)) }
    fun onEstado(v: EstadoSector) = _estado.update { it.copy(estado = v) }

    fun guardar() {
        // Control de acceso real: no basta con ocultar botones
        val sesion = SesionManager.sesion.value
        if (!sesion.puedeAcceder(Perfil.ADMINISTRADOR)) {
            _estado.update { it.copy(errorGeneral = "No tienes permiso para administrar sectores") }
            return
        }

        val actual = _estado.value
        val errores = validarSector(actual.codigo, actual.nombre, actual.ubicacion, actual.descripcion)
        if (errores.hayErrores) {
            _estado.update { it.copy(errores = errores, errorGeneral = null) }
            return
        }

        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, errorGeneral = null) }
            val codigo = actual.codigo.trim().uppercase()
            val idExcluido = original?.id ?: 0

            if (repo.codigoDuplicado(codigo, idExcluido)) {
                _estado.update {
                    it.copy(
                        cargando = false,
                        errores = it.errores.copy(codigo = "Ya existe un sector con ese código")
                    )
                }
                return@launch
            }

            val ahora = System.currentTimeMillis()
            val base = original
            repo.guardar(
                Sector(
                    id = base?.id ?: 0,
                    codigo = codigo,
                    nombre = actual.nombre.trim(),
                    descripcion = actual.descripcion.trim(),
                    ubicacion = actual.ubicacion.trim(),
                    estado = actual.estado,
                    responsableId = base?.responsableId ?: sesion?.usuarioId,
                    fechaCreacion = base?.fechaCreacion ?: ahora,
                    fechaActualizacion = ahora
                )
            )
            _estado.update { it.copy(cargando = false, guardado = true) }
        }
    }

    fun eliminar() {
        val sesion = SesionManager.sesion.value
        if (!sesion.puedeAcceder(Perfil.ADMINISTRADOR)) {
            _estado.update { it.copy(errorGeneral = "No tienes permiso para eliminar sectores") }
            return
        }
        val sector = original ?: return
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, errorGeneral = null) }
            // Regla de negocio: no se borra un sector con datos asociados
            val motivo = motivoNoEliminarSector(repo.dependencias(sector.id))
            if (motivo != null) {
                _estado.update { it.copy(cargando = false, errorGeneral = motivo) }
                return@launch
            }
            try {
                repo.eliminar(sector)
                _estado.update { it.copy(cargando = false, eliminado = true) }
            } catch (e: Exception) {
                // Respaldo: la clave foránea (RESTRICT) también lo impide
                _estado.update { it.copy(cargando = false, errorGeneral = "No se pudo eliminar: el sector tiene datos asociados") }
            }
        }
    }

    companion object {
        fun factory(app: Application, sectorId: Int) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                SectorFormViewModel(
                    AppContainer.sectorRepository(app),
                    sectorId
                ) as T
        }
    }
}
