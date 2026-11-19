package cl.duoc.huerto.domain.repository

import cl.duoc.huerto.domain.model.ResultadoSincronizacion

/**
 * Contrato para traer los datos del backend al teléfono.
 * Devuelve Result: éxito con el resumen, o fallo si el servidor no respondió.
 */
interface SincronizacionRepository {
    suspend fun sincronizar(): Result<ResultadoSincronizacion>
}
