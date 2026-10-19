package cl.duoc.huerto.domain

/** Longitudes máximas: la UI las usa para el contador y los validadores para rechazar. */
object LimitesCampo {
    const val SECTOR_CODIGO = 10
    const val SECTOR_NOMBRE = 40
    const val SECTOR_UBICACION = 60
    const val SECTOR_DESCRIPCION = 200

    const val CULTIVO_NOMBRE = 40
    const val CULTIVO_DESCRIPCION = 200
    const val RIEGO_MAX_DIAS = 30

    const val TAREA_DESCRIPCION = 300
    const val TAREA_DESCRIPCION_MIN = 10
    const val COMENTARIO = 300
    const val MOTIVO_RECHAZO_MIN = 10

    const val PROBLEMA_DESCRIPCION = 300

    const val USUARIO = 30
    const val PASSWORD = 30

    const val MIN_TEXTO = 3
}

/**
 * Reglas reutilizables: devuelven el mensaje de error, o null si es válido.
 * `campo` se escribe con su artículo: "el nombre", "la ubicación".
 */
object Reglas {
    fun requerido(valor: String, campo: String): String? =
        if (valor.isBlank()) "Ingresa $campo" else null

    /** Solo revisa el largo si hay texto; si el campo es obligatorio, usa requeridoYLargo. */
    @Suppress("UNUSED_PARAMETER")
    fun largo(valor: String, campo: String, max: Int, min: Int = 0): String? = when {
        valor.isNotBlank() && valor.trim().length < min -> "Escribe al menos $min caracteres"
        valor.length > max -> "Máximo $max caracteres"
        else -> null
    }

    fun requeridoYLargo(valor: String, campo: String, max: Int, min: Int = LimitesCampo.MIN_TEXTO) =
        requerido(valor, campo) ?: largo(valor, campo, max, min)

    fun enteroPositivo(valor: String, campo: String, max: Int): String? {
        if (valor.isBlank()) return "Ingresa $campo"
        val n = valor.toIntOrNull() ?: return "Debe ser un número entero"
        return when {
            n <= 0 -> "Debe ser mayor que 0"
            n > max -> "No puede ser mayor que $max"
            else -> null
        }
    }
}