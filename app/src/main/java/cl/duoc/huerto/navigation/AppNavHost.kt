package cl.duoc.huerto.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cl.duoc.huerto.u_i.PantallaPrincipal
import cl.duoc.huerto.u_i.auth.LoginScreen

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