package cl.duoc.huerto.data.repository

import cl.duoc.huerto.data.local.dao.SincronizacionDao
import cl.duoc.huerto.data.remote.HuertoApi
import cl.duoc.huerto.domain.model.ResultadoSincronizacion
import cl.duoc.huerto.domain.repository.SincronizacionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

/**
 * Descarga todo desde huertoapi y lo guarda en Room.
 * Las pantallas siguen leyendo SOLO de Room (Flow), así que se actualizan solas
 * al terminar y la app funciona igual sin conexión.
 */
class SincronizacionRepositoryImpl(
    private val api: HuertoApi,
    private val dao: SincronizacionDao
) : SincronizacionRepository {

    override suspend fun sincronizar(): Result<ResultadoSincronizacion> = withContext(Dispatchers.IO) {
        try {
            // 1) Descargar (si algo falla aquí, no se guarda nada)
            val usuarios = api.usuarios().mapNotNull { it.toEntity() }
            val sectores = api.sectores().mapNotNull { it.toEntity() }
            val cultivos = api.cultivos().mapNotNull { it.toEntity() }
            val tareas = api.tareas().mapNotNull { it.toEntity() }
            val problemas = api.problemas().mapNotNull { it.toEntity() }

            // 2) Guardar todo en una sola transacción
            dao.guardarTodo(usuarios, sectores, cultivos, tareas, problemas)

            Result.success(
                ResultadoSincronizacion(
                    usuarios.size, sectores.size, cultivos.size, tareas.size, problemas.size
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
