package cl.duoc.huerto.data.repository

import cl.duoc.huerto.data.remote.clima.ClimaApi
import cl.duoc.huerto.domain.model.Clima
import cl.duoc.huerto.domain.repository.ClimaRepository
import kotlin.coroutines.cancellation.CancellationException

/**
 * Consulta el clima en la API externa. No guarda nada en Room ni toca el microservicio:
 * es información de apoyo que se muestra en el Inicio.
 * Coordenadas por defecto: Santiago de Chile.
 */
class ClimaRepositoryImpl(
    private val api: ClimaApi,
    private val latitud: Double = -33.4489,
    private val longitud: Double = -70.6693
) : ClimaRepository {

    override suspend fun climaActual(): Result<Clima> = try {
        val clima = api.pronostico(latitud, longitud).toDomain()
        if (clima != null) Result.success(clima)
        else Result.failure(IllegalStateException("Respuesta sin clima actual"))
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}
