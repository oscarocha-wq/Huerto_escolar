package cl.duoc.huerto.data.remote

import kotlin.coroutines.cancellation.CancellationException

/**
 * Ejecuta una llamada al microservicio sin botar la app si falla
 * (servidor apagado, sin red, error 4xx/5xx). Devuelve null en ese caso.
 * Así la app sigue funcionando con Room aunque el backend no responda.
 */
suspend fun <T> intentarRemoto(llamada: suspend () -> T): T? = try {
    llamada()
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    null
}
