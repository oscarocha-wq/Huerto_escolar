package cl.duoc.huerto.domain

data class ErroresSector(
    val codigo: String? = null,
    val nombre: String? = null,
    val ubicacion: String? = null,
    val descripcion: String? = null
) {
    val hayErrores: Boolean
        get() = listOf(codigo, nombre, ubicacion, descripcion).any { it != null }
}

/** Código: 1 a 3 letras, guion y 1 a 3 números (ej: S-03). */
val FORMATO_CODIGO_SECTOR = Regex("^[A-Za-z]{1,3}-\\d{1,3}$")

fun validarSector(
    codigo: String,
    nombre: String,
    ubicacion: String,
    descripcion: String = ""
) = ErroresSector(
    codigo = Reglas.requerido(codigo, "el código")
        ?: if (!FORMATO_CODIGO_SECTOR.matches(codigo.trim())) "Usa el formato S-03 (letras, guion y número)" else null,
    nombre = Reglas.requeridoYLargo(nombre, "el nombre", LimitesCampo.SECTOR_NOMBRE),
    ubicacion = Reglas.requeridoYLargo(ubicacion, "la ubicación", LimitesCampo.SECTOR_UBICACION),
    descripcion = Reglas.largo(descripcion, "la descripción", LimitesCampo.SECTOR_DESCRIPCION)
)