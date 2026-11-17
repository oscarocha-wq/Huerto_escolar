package cl.duoc.huerto.presentation.screens.problemas

import cl.duoc.huerto.domain.model.Cultivo
import cl.duoc.huerto.domain.model.Sector
import cl.duoc.huerto.domain.model.TipoProblema
import cl.duoc.huerto.domain.validation.ErroresProblema

data class ProblemaFormState(
    val tipo: TipoProblema? = null,
    val descripcion: String = "",
    val sectorId: Int? = null,
    val cultivoId: Int? = null,
    val fotoRuta: String? = null,
    val sectores: List<Sector> = emptyList(),
    val cultivos: List<Cultivo> = emptyList(),
    val errores: ErroresProblema = ErroresProblema(),
    val errorGeneral: String? = null,
    val cargando: Boolean = false,
    val guardado: Boolean = false
)
