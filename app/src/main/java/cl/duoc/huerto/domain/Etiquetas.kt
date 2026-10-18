package cl.duoc.huerto.domain

fun EstadoSector.etiqueta() = when (this) {
    EstadoSector.DISPONIBLE -> "Disponible"
    EstadoSector.EN_USO -> "En uso"
    EstadoSector.EN_MANTENIMIENTO -> "En mantenimiento"
    EstadoSector.FUERA_DE_USO -> "Fuera de uso"
}
fun EstadoCultivo.etiqueta() = when (this) {
    EstadoCultivo.PLANIFICADO -> "Planificado"
    EstadoCultivo.SEMBRADO -> "Sembrado"
    EstadoCultivo.EN_CRECIMIENTO -> "En crecimiento"
    EstadoCultivo.LISTO_PARA_COSECHAR -> "Listo para cosechar"
    EstadoCultivo.COSECHADO -> "Cosechado"
    EstadoCultivo.CON_PROBLEMAS -> "Con problemas"
    EstadoCultivo.FINALIZADO -> "Finalizado"
}

fun EstadoTarea.etiqueta() = when (this) {
    EstadoTarea.PENDIENTE -> "Pendiente"
    EstadoTarea.EN_DESARROLLO -> "En desarrollo"
    EstadoTarea.REALIZADA -> "Realizada"
    EstadoTarea.PENDIENTE_DE_VALIDACION -> "Pendiente de validación"
    EstadoTarea.VALIDADA -> "Validada"
    EstadoTarea.RECHAZADA -> "Rechazada"
}

fun TipoTarea.etiqueta() = when (this) {
    TipoTarea.PREPARACION_SECTOR -> "Preparación del sector"
    TipoTarea.SIEMBRA -> "Siembra"
    TipoTarea.TRASPLANTE -> "Trasplante"
    TipoTarea.RIEGO -> "Riego"
    TipoTarea.LIMPIEZA -> "Limpieza"
    TipoTarea.RETIRO_MALEZAS -> "Retiro de malezas"
    TipoTarea.OBSERVACION -> "Observación del cultivo"
    TipoTarea.MANTENIMIENTO -> "Mantenimiento"
    TipoTarea.REVISION_SUELO -> "Revisión del suelo"
    TipoTarea.REGISTRO_CRECIMIENTO -> "Registro del crecimiento"
    TipoTarea.COSECHA -> "Cosecha"
    TipoTarea.OTRA -> "Otra actividad autorizada"
}

fun TipoProblema.etiqueta() = when (this) {
    TipoProblema.FALTA_AGUA -> "Falta de agua"
    TipoProblema.DANO_PLANTA -> "Daño en la planta"
    TipoProblema.PLAGA_SIMULADA -> "Plaga simulada"
    TipoProblema.DETERIORO_SECTOR -> "Deterioro del sector"
    TipoProblema.MANTENIMIENTO -> "Problema de mantenimiento"
    TipoProblema.OTRO -> "Otro problema observado"
}

fun EstadoProblema.etiqueta() = when (this) {
    EstadoProblema.INFORMADO -> "Informado"
    EstadoProblema.EN_REVISION -> "En revisión"
    EstadoProblema.ATENDIDO -> "Atendido"
    EstadoProblema.CERRADO -> "Cerrado"
}