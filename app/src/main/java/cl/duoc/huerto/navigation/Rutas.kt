package cl.duoc.huerto.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector
import cl.duoc.huerto.domain.Perfil

object Rutas {
    const val LOGIN = "login"
    const val PRINCIPAL = "principal"

    // Compartidas
    const val INICIO = "inicio"
    const val ALERTAS = "alertas"
    const val HISTORIAL = "historial"

    // Administrador
    const val SECTORES = "sectores"
    const val SECTOR_FORM = "sector_form/{id}"
    fun sectorForm(id: Int) = "sector_form/$id"
    const val CULTIVOS = "cultivos"
    const val CULTIVO_FORM = "cultivo_form/{id}"
    fun cultivoForm(id: Int) = "cultivo_form/$id"
    const val TAREAS_ADMIN = "tareas_admin"
    const val TAREA_FORM = "tarea_form"
    const val PROBLEMAS_ADMIN = "problemas_admin"

    // Estudiante
    const val MIS_TAREAS = "mis_tareas"
    const val REPORTAR = "reportar"
    const val PROBLEMA_FORM = "problema_form"

    // Detalles (ambos perfiles)
    const val TAREA_DETALLE = "tarea_detalle/{id}"
    fun tareaDetalle(id: Int) = "tarea_detalle/$id"
    const val PROBLEMA_DETALLE = "problema_detalle/{id}"
    fun problemaDetalle(id: Int) = "problema_detalle/$id"
}

data class ItemMenu(val ruta: String, val titulo: String, val icono: ImageVector)

val menuAdministrador = listOf(
    ItemMenu(Rutas.INICIO, "Inicio", Icons.Filled.Home),
    ItemMenu(Rutas.SECTORES, "Sectores", Icons.Filled.Grass),
    ItemMenu(Rutas.CULTIVOS, "Cultivos", Icons.Filled.Eco),
    ItemMenu(Rutas.TAREAS_ADMIN, "Tareas", Icons.Filled.Assignment),
    ItemMenu(Rutas.PROBLEMAS_ADMIN, "Problemas", Icons.Filled.Warning)
)

val menuEstudiante = listOf(
    ItemMenu(Rutas.INICIO, "Inicio", Icons.Filled.Home),
    ItemMenu(Rutas.MIS_TAREAS, "Mis tareas", Icons.Filled.Agriculture),
    ItemMenu(Rutas.REPORTAR, "Reportar", Icons.Filled.ReportProblem)
)

fun menuPara(perfil: Perfil) =
    if (perfil == Perfil.ADMINISTRADOR) menuAdministrador else menuEstudiante

fun inicioPara(perfil: Perfil) = menuPara(perfil).first().ruta