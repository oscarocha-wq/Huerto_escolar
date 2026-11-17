package cl.duoc.huerto.presentation.screens.sensores

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.duoc.huerto.core.di.AppContainer
import cl.duoc.huerto.core.session.SesionManager
import cl.duoc.huerto.domain.model.*
import cl.duoc.huerto.domain.repository.LecturaRepository
import cl.duoc.huerto.domain.repository.SectorRepository
import cl.duoc.huerto.domain.rules.*
import cl.duoc.huerto.domain.usecase.*
import cl.duoc.huerto.domain.validation.parseNumero
import cl.duoc.huerto.domain.validation.validarLectura
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SensoresViewModel(
    sectorRepo: SectorRepository,
    private val repo: LecturaRepository,
    private val sectorId: Int
) : ViewModel() {

    val sector: StateFlow<Sector?> = sectorRepo.observar(sectorId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** null = cargando. De la más reciente a la más antigua. */
    val lecturas: StateFlow<List<LecturaSensor>?> = repo.deSector(sectorId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _form = MutableStateFlow(FormLecturaUi())
    val form: StateFlow<FormLecturaUi> = _form.asStateFlow()

    fun onHumedadSuelo(v: String) = _form.update { it.copy(humedadSuelo = v, errores = it.errores.copy(humedadSuelo = null)) }
    fun onTemperatura(v: String) = _form.update { it.copy(temperatura = v, errores = it.errores.copy(temperatura = null)) }
    fun onHumedadAmbiente(v: String) = _form.update { it.copy(humedadAmbiente = v, errores = it.errores.copy(humedadAmbiente = null)) }
    fun mensajeMostrado() = _form.update { it.copy(mensaje = null) }

    private fun esAdmin(): Boolean {
        val ok = SesionManager.sesion.value.puedeAcceder(Perfil.ADMINISTRADOR)
        if (!ok) _form.update { it.copy(error = "No tienes permiso para registrar lecturas") }
        return ok
    }

    /** Genera un dato de prueba (simulación académica, no hay sensor real). */
    fun simular() {
        if (!esAdmin()) return
        viewModelScope.launch {
            repo.guardar(simularLectura(sectorId, System.currentTimeMillis()))
            _form.update { it.copy(mensaje = "Lectura de prueba generada", error = null) }
        }
    }

    fun registrarManual() {
        if (!esAdmin()) return
        val f = _form.value
        val errores = validarLectura(f.humedadSuelo, f.temperatura, f.humedadAmbiente)
        if (errores.hayErrores) {
            _form.update { it.copy(errores = errores, error = null) }
            return
        }
        viewModelScope.launch {
            _form.update { it.copy(procesando = true) }
            repo.guardar(
                LecturaSensor(
                    sectorId = sectorId,
                    fecha = System.currentTimeMillis(),
                    humedadSuelo = parseNumero(f.humedadSuelo) ?: 0.0,
                    temperatura = parseNumero(f.temperatura) ?: 0.0,
                    humedadAmbiente = parseNumero(f.humedadAmbiente) ?: 0.0,
                    origen = OrigenLectura.MANUAL
                )
            )
            _form.value = FormLecturaUi(mensaje = "Lectura registrada")
        }
    }

    fun borrarSimuladas() {
        if (!esAdmin()) return
        viewModelScope.launch {
            val n = repo.borrarSimuladas(sectorId)
            _form.update { it.copy(mensaje = "Se borraron $n dato(s) de prueba", error = null) }
        }
    }

    companion object {
        fun factory(app: Application, sectorId: Int) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SensoresViewModel(
                    AppContainer.sectorRepository(app),
                    AppContainer.lecturaRepository(app),
                    sectorId
                ) as T
            }
        }
    }
}
