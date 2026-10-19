package cl.duoc.huerto.u_i.sensores

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.duoc.huerto.data.AppDatabase
import cl.duoc.huerto.data.SesionManager
import cl.duoc.huerto.data.entities.LecturaSensor
import cl.duoc.huerto.data.entities.Sector
import cl.duoc.huerto.data.repository.LecturaRepository
import cl.duoc.huerto.data.repository.SectorRepository
import cl.duoc.huerto.domain.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class FormLecturaUi(
    val humedadSuelo: String = "",
    val temperatura: String = "",
    val humedadAmbiente: String = "",
    val errores: ErroresLectura = ErroresLectura(),
    val error: String? = null,
    val mensaje: String? = null,
    val procesando: Boolean = false
)

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
                val db = AppDatabase.get(app)
                return SensoresViewModel(
                    SectorRepository(db.sectorDao()),
                    LecturaRepository(db.lecturaDao()),
                    sectorId
                ) as T
            }
        }
    }
}
