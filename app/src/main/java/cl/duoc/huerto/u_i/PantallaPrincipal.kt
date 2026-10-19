package cl.duoc.huerto.u_i

import android.app.Application
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.duoc.huerto.data.SesionManager
import cl.duoc.huerto.domain.Alerta
import cl.duoc.huerto.domain.EventoHistorial
import cl.duoc.huerto.domain.Perfil
import cl.duoc.huerto.navigation.*
import cl.duoc.huerto.u_i.avisos.AvisosNotificacion
import cl.duoc.huerto.u_i.cultivos.CultivoFormScreen
import cl.duoc.huerto.u_i.cultivos.CultivosScreen
import cl.duoc.huerto.u_i.cultivos.NUEVO_CULTIVO
import cl.duoc.huerto.u_i.panel.AlertasScreen
import cl.duoc.huerto.u_i.panel.HistorialScreen
import cl.duoc.huerto.u_i.panel.InicioScreen
import cl.duoc.huerto.u_i.panel.PanelViewModel
import cl.duoc.huerto.u_i.problemas.ProblemaDetalleScreen
import cl.duoc.huerto.u_i.problemas.ProblemaFormScreen
import cl.duoc.huerto.u_i.problemas.ProblemasScreen
import cl.duoc.huerto.u_i.sectores.NUEVO_SECTOR
import cl.duoc.huerto.u_i.sectores.SectorDetalleScreen
import cl.duoc.huerto.u_i.sectores.SectorFormScreen
import cl.duoc.huerto.u_i.sectores.SectoresScreen
import cl.duoc.huerto.u_i.sensores.SensoresScreen
import cl.duoc.huerto.u_i.tareas.TareaDetalleScreen
import cl.duoc.huerto.u_i.tareas.TareaFormScreen
import cl.duoc.huerto.u_i.tareas.TareasScreen

/** Desde 600 dp de ancho (tablet o celular horizontal) se usa navegación lateral. */
private const val ANCHO_PANTALLA_GRANDE = 600

/** Ancho máximo del contenido para que formularios y listas sigan siendo legibles. */
private val ANCHO_MAXIMO_CONTENIDO = 840.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPrincipal(onCerrarSesion: () -> Unit) {
    val context = LocalContext.current
    val sesion by SesionManager.sesion.collectAsStateWithLifecycle()
    val panelVm: PanelViewModel = viewModel(
        factory = PanelViewModel.factory(context.applicationContext as Application)
    )
    val panel by panelVm.ui.collectAsStateWithLifecycle()
    val perfil = sesion?.perfil ?: return   // sin sesión no se dibuja nada

    AvisosNotificacion()

    val navController = rememberNavController()
    val entrada by navController.currentBackStackEntryAsState()
    val destinoActual = entrada?.destination
    val menu = menuPara(perfil)
    val pantallaAncha = LocalConfiguration.current.screenWidthDp >= ANCHO_PANTALLA_GRANDE

    fun irA(ruta: String) = navController.navigate(ruta) { launchSingleTop = true }

    /** Navegación del menú principal: conserva el estado de cada sección. */
    fun irASeccion(ruta: String) = navController.navigate(ruta) {
        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }

    fun estaSeleccionado(ruta: String) =
        destinoActual?.hierarchy?.any { it.route == ruta } == true

    fun abrirAlerta(a: Alerta) {
        when {
            a.tareaId != null -> navController.navigate(Rutas.tareaDetalle(a.tareaId))
            a.problemaId != null -> navController.navigate(Rutas.problemaDetalle(a.problemaId))
        }
    }

    fun abrirEvento(e: EventoHistorial) {
        when {
            e.tareaId != null -> navController.navigate(Rutas.tareaDetalle(e.tareaId))
            e.problemaId != null -> navController.navigate(Rutas.problemaDetalle(e.problemaId))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(sesion?.nombre ?: "", style = MaterialTheme.typography.titleMedium)
                        Text(
                            if (perfil == Perfil.ADMINISTRADOR) "Administrador" else "Estudiante",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { irA(Rutas.ALERTAS) }) {
                        BadgedBox(badge = {
                            val n = panel.alertas.size
                            if (n > 0) Badge { Text(if (n > 9) "9+" else "$n") }
                        }) {
                            Icon(Icons.Filled.Notifications, contentDescription = "Alertas")
                        }
                    }
                    IconButton(onClick = { irA(Rutas.HISTORIAL) }) {
                        Icon(Icons.Filled.History, contentDescription = "Historial")
                    }
                    IconButton(onClick = {
                        SesionManager.cerrar()
                        onCerrarSesion()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar sesión")
                    }
                }
            )
        },
        bottomBar = {
            // Celular vertical: barra inferior
            if (!pantallaAncha) {
                NavigationBar {
                    menu.forEach { item ->
                        NavigationBarItem(
                            selected = estaSeleccionado(item.ruta),
                            onClick = { irASeccion(item.ruta) },
                            icon = { Icon(item.icono, contentDescription = item.titulo) },
                            label = { Text(item.titulo) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Row(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tablet u horizontal: riel lateral
            if (pantallaAncha) {
                NavigationRail {
                    Spacer(Modifier.height(8.dp))
                    menu.forEach { item ->
                        NavigationRailItem(
                            selected = estaSeleccionado(item.ruta),
                            onClick = { irASeccion(item.ruta) },
                            icon = { Icon(item.icono, contentDescription = item.titulo) },
                            label = { Text(item.titulo) }
                        )
                    }
                }
            }

            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.TopCenter
            ) {
                NavHost(
                    navController = navController,
                    startDestination = inicioPara(perfil),
                    modifier = Modifier
                        .widthIn(max = ANCHO_MAXIMO_CONTENIDO)
                        .fillMaxSize(),
                    enterTransition = { fadeIn(tween(300)) + slideInHorizontally(tween(300)) { it / 6 } },
                    exitTransition = { fadeOut(tween(200)) },
                    popEnterTransition = { fadeIn(tween(300)) },
                    popExitTransition = { fadeOut(tween(200)) + slideOutHorizontally(tween(300)) { it / 6 } }
                ) {
                    // --- Compartidas ---
                    composable(Rutas.INICIO) {
                        RutaProtegida(null) {
                            InicioScreen(
                                ui = panel,
                                onVerAlertas = { irA(Rutas.ALERTAS) },
                                onVerHistorial = { irA(Rutas.HISTORIAL) },
                                onAbrirAlerta = ::abrirAlerta
                            )
                        }
                    }
                    composable(Rutas.ALERTAS) {
                        RutaProtegida(null) {
                            AlertasScreen(
                                alertas = panel.alertas,
                                onAbrirAlerta = ::abrirAlerta,
                                onVolver = { navController.popBackStack() }
                            )
                        }
                    }
                    composable(Rutas.HISTORIAL) {
                        RutaProtegida(null) {
                            HistorialScreen(
                                eventos = panel.historial,
                                onAbrirEvento = ::abrirEvento,
                                onVolver = { navController.popBackStack() }
                            )
                        }
                    }
                    composable(
                        route = Rutas.TAREA_DETALLE,
                        arguments = listOf(navArgument("id") { type = NavType.IntType })
                    ) { e ->
                        val id = e.arguments?.getInt("id") ?: -1
                        RutaProtegida(null) {
                            TareaDetalleScreen(tareaId = id, onVolver = { navController.popBackStack() })
                        }
                    }
                    composable(
                        route = Rutas.PROBLEMA_DETALLE,
                        arguments = listOf(navArgument("id") { type = NavType.IntType })
                    ) { e ->
                        val id = e.arguments?.getInt("id") ?: -1
                        RutaProtegida(null) {
                            ProblemaDetalleScreen(problemaId = id, onVolver = { navController.popBackStack() })
                        }
                    }

                    // --- Solo administrador ---
                    composable(Rutas.SECTORES) {
                        RutaProtegida(Perfil.ADMINISTRADOR) {
                            SectoresScreen(
                                onNuevo = { navController.navigate(Rutas.sectorForm(NUEVO_SECTOR)) },
                                onEditar = { id -> navController.navigate(Rutas.sectorDetalle(id)) }
                            )
                        }
                    }
                    composable(
                        route = Rutas.SECTOR_FORM,
                        arguments = listOf(navArgument("id") { type = NavType.IntType })
                    ) { e ->
                        val id = e.arguments?.getInt("id") ?: NUEVO_SECTOR
                        RutaProtegida(Perfil.ADMINISTRADOR) {
                            SectorFormScreen(sectorId = id, onVolver = { navController.popBackStack() })
                        }
                    }
                    composable(
                        route = Rutas.SECTOR_DETALLE,
                        arguments = listOf(navArgument("id") { type = NavType.IntType })
                    ) { e ->
                        val id = e.arguments?.getInt("id") ?: -1
                        RutaProtegida(Perfil.ADMINISTRADOR) {
                            SectorDetalleScreen(
                                sectorId = id,
                                onVolver = { navController.popBackStack() },
                                onEditar = { navController.navigate(Rutas.sectorForm(it)) },
                                onAbrirCultivo = { navController.navigate(Rutas.cultivoForm(it)) },
                                onVerSensores = { navController.navigate(Rutas.sensores(it)) }
                            )
                        }
                    }
                    composable(
                        route = Rutas.SENSORES,
                        arguments = listOf(navArgument("id") { type = NavType.IntType })
                    ) { e ->
                        val id = e.arguments?.getInt("id") ?: -1
                        RutaProtegida(Perfil.ADMINISTRADOR) {
                            SensoresScreen(sectorId = id, onVolver = { navController.popBackStack() })
                        }
                    }
                    composable(Rutas.CULTIVOS) {
                        RutaProtegida(Perfil.ADMINISTRADOR) {
                            CultivosScreen(
                                onNuevo = { navController.navigate(Rutas.cultivoForm(NUEVO_CULTIVO)) },
                                onEditar = { id -> navController.navigate(Rutas.cultivoForm(id)) }
                            )
                        }
                    }
                    composable(
                        route = Rutas.CULTIVO_FORM,
                        arguments = listOf(navArgument("id") { type = NavType.IntType })
                    ) { e ->
                        val id = e.arguments?.getInt("id") ?: NUEVO_CULTIVO
                        RutaProtegida(Perfil.ADMINISTRADOR) {
                            CultivoFormScreen(cultivoId = id, onVolver = { navController.popBackStack() })
                        }
                    }
                    composable(Rutas.TAREAS_ADMIN) {
                        RutaProtegida(Perfil.ADMINISTRADOR) {
                            TareasScreen(
                                onNueva = { navController.navigate(Rutas.TAREA_FORM) },
                                onAbrir = { id -> navController.navigate(Rutas.tareaDetalle(id)) }
                            )
                        }
                    }
                    composable(Rutas.TAREA_FORM) {
                        RutaProtegida(Perfil.ADMINISTRADOR) {
                            TareaFormScreen(onVolver = { navController.popBackStack() })
                        }
                    }
                    composable(Rutas.PROBLEMAS_ADMIN) {
                        RutaProtegida(Perfil.ADMINISTRADOR) {
                            ProblemasScreen(
                                onNuevo = null,
                                onAbrir = { id -> navController.navigate(Rutas.problemaDetalle(id)) }
                            )
                        }
                    }

                    // --- Solo estudiante ---
                    composable(Rutas.MIS_TAREAS) {
                        RutaProtegida(Perfil.ESTUDIANTE) {
                            TareasScreen(
                                onNueva = null,
                                onAbrir = { id -> navController.navigate(Rutas.tareaDetalle(id)) }
                            )
                        }
                    }
                    composable(Rutas.REPORTAR) {
                        RutaProtegida(Perfil.ESTUDIANTE) {
                            ProblemasScreen(
                                onNuevo = { navController.navigate(Rutas.PROBLEMA_FORM) },
                                onAbrir = { id -> navController.navigate(Rutas.problemaDetalle(id)) }
                            )
                        }
                    }
                    composable(Rutas.PROBLEMA_FORM) {
                        RutaProtegida(Perfil.ESTUDIANTE) {
                            ProblemaFormScreen(onVolver = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }
}
