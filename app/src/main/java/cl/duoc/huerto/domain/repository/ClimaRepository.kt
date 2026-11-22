package cl.duoc.huerto.domain.repository

import cl.duoc.huerto.domain.model.Clima

/** Contrato para obtener el clima desde la API externa. */
interface ClimaRepository {
    suspend fun climaActual(): Result<Clima>
}
