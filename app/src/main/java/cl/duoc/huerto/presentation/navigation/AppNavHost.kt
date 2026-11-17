package cl.duoc.huerto.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cl.duoc.huerto.presentation.screens.auth.LoginScreen
import cl.duoc.huerto.presentation.screens.main.PantallaPrincipal

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Rutas.LOGIN,
        enterTransition = { fadeIn(tween(400)) },
        exitTransition = { fadeOut(tween(300)) }
    ) {
        composable(Rutas.LOGIN) {
            LoginScreen(onLoginExitoso = {
                navController.navigate(Rutas.PRINCIPAL) {
                    popUpTo(Rutas.LOGIN) { inclusive = true }
                }
            })
        }
        composable(Rutas.PRINCIPAL) {
            PantallaPrincipal(onCerrarSesion = {
                navController.navigate(Rutas.LOGIN) {
                    popUpTo(Rutas.PRINCIPAL) { inclusive = true }
                }
            })
        }
    }
}
