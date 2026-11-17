package cl.duoc.huerto.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.ui.graphics.vector.ImageVector
import cl.duoc.huerto.domain.model.TipoProblema
import cl.duoc.huerto.domain.model.TipoTarea

/** Ícono que representa cada tipo de tarea en las tarjetas. */
fun TipoTarea.icono(): ImageVector = when (this) {
    TipoTarea.PREPARACION_SECTOR -> Icons.Filled.Construction
    TipoTarea.SIEMBRA -> Icons.Filled.Spa
    TipoTarea.TRASPLANTE -> Icons.Filled.SwapHoriz
    TipoTarea.RIEGO -> Icons.Filled.WaterDrop
    TipoTarea.LIMPIEZA -> Icons.Filled.CleaningServices
    TipoTarea.RETIRO_MALEZAS -> Icons.Filled.ContentCut
    TipoTarea.OBSERVACION -> Icons.Filled.Visibility
    TipoTarea.MANTENIMIENTO -> Icons.Filled.Build
    TipoTarea.REVISION_SUELO -> Icons.Filled.Science
    TipoTarea.REGISTRO_CRECIMIENTO -> Icons.Filled.Straighten
    TipoTarea.COSECHA -> Icons.Filled.Agriculture
    TipoTarea.OTRA -> Icons.AutoMirrored.Filled.Assignment
}

/** Ícono que representa cada tipo de problema en las tarjetas. */
fun TipoProblema.icono(): ImageVector = when (this) {
    TipoProblema.FALTA_AGUA -> Icons.Filled.WaterDrop
    TipoProblema.DANO_PLANTA -> Icons.Filled.Healing
    TipoProblema.PLAGA_SIMULADA -> Icons.Filled.BugReport
    TipoProblema.DETERIORO_SECTOR -> Icons.Filled.Warning
    TipoProblema.MANTENIMIENTO -> Icons.Filled.Build
    TipoProblema.OTRO -> Icons.Filled.ReportProblem
}
