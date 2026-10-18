package cl.duoc.huerto.domain

data class ErroresSector(
    val codigo: String? = null,
    val nombre: String? = null
) {
    val hayErrores: Boolean get() = codigo != null || nombre != null
}

fun validarSector(codigo: String, nombre: String) = ErroresSector(
    codigo = if (codigo.isBlank()) "El código es obligatorio" else null,
    nombre = if (nombre.isBlank()) "El nombre es obligatorio" else null
)