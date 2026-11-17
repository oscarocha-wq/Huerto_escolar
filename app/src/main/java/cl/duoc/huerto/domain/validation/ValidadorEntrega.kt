package cl.duoc.huerto.domain.validation

/** Una entrega necesita al menos una foto o un comentario como evidencia. */
fun validarEntrega(comentario: String, fotoRuta: String?): String? = when {
    comentario.isBlank() && fotoRuta == null -> "Agrega una foto o un comentario como evidencia"
    comentario.length > LimitesCampo.COMENTARIO -> "Máximo ${LimitesCampo.COMENTARIO} caracteres"
    else -> null
}
