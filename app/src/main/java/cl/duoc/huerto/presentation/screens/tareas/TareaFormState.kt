package cl.duoc.huerto.presentation.screens.tareas

import cl.duoc.huerto.domain.model.Cultivo
import cl.duoc.huerto.domain.model.Sector
import cl.duoc.huerto.domain.model.TipoTarea
import cl.duoc.huerto.domain.model.Usuario
import cl.duoc.huerto.domain.validation.ErroresTarea

enum class ModoAsignacion { ESTUDIANTE, GRUPO }

data class TareaFormState(
    val tipo: TipoTarea? = null,
    val descripcion: String = "",
    val sectorId: Int? = null,
    val cultivoId: Int? = null,
    val fechaProgramada: Long? = null,
    val modo: ModoAsignacion = ModoAsignacion.ESTUDIANTE,
    val estudianteId: String? = null,
    val grupo: String? = null,
    val sectores: List<Sector> = emptyList(),
    val cultivos: List<Cultivo> = emptyList(),
    val estudiantes: List<Usuario> = emptyList(),
    val errores: ErroresTarea = ErroresTarea(),
    val errorGeneral: String? = null,
    val cargando: Boolean = false,
    val guardado: Boolean = false
) {
    val grupos: List<String> get() = estudiantes.mapNotNull { it.grupo }.distinct().sorted()
}
