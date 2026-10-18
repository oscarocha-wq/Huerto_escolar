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
    const val COMENTARIO = 300
    const val MOTIVO_RECHAZO_MIN = 10

    const val PROBLEMA_DESCRIPCION = 300

    const val USUARIO = 30
    const val PASSWORD = 30

    const val MIN_TEXTO = 3
}

/** Reglas reutilizables: devuelven el mensaje de error, o null si es válido. */
object Reglas {
    fun requerido(valor: String, campo: String): String? =
        if (valor.isBlank()) "$campo es obligatorio" else null

    fun largo(valor: String, campo: String, max: Int, min: Int = 0): String? = when {
        valor.trim().length < min -> "$campo debe tener al menos $min caracteres"
        valor.length > max -> "$campo no puede superar $max caracteres"
        else -> null
    }

    fun requeridoYLargo(valor: String, campo: String, max: Int, min: Int = LimitesCampo.MIN_TEXTO) =
        requerido(valor, campo) ?: largo(valor, campo, max, min)

    fun enteroPositivo(valor: String, campo: String, max: Int): String? {
        if (valor.isBlank()) return "$campo es obligatorio"
        val n = valor.toIntOrNull() ?: return "$campo debe ser un número entero"
        return when {
            n <= 0 -> "$campo debe ser mayor que 0"
            n > max -> "$campo no puede ser mayor que $max"
            else -> null
        }
    }
}