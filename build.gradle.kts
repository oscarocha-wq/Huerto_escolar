// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    // Kover en la raíz: junta la cobertura de todos los módulos (hoy solo :app)
    id("org.jetbrains.kotlinx.kover") version "0.9.11"
}

// ---------- Kover a nivel de proyecto (raíz) ----------
// Informe de TODO el proyecto:  gradlew koverHtmlReport  -> build/reports/kover/html/index.html
// Informe XML:                  gradlew koverXmlReport
// Verificar mínimo (80 %):      gradlew koverVerify
dependencies {
    kover(project(":app"))
}

kover {
    reports {
        filters {
            excludes {
                // Mismas exclusiones que app/build.gradle.kts (interfaz Compose y código de Android)
                annotatedBy("androidx.compose.runtime.Composable")
                classes(
                    "*ComposableSingletons*",
                    "*ScreenKt*",
                    "cl.duoc.huerto.presentation.components.*",
                    "cl.duoc.huerto.presentation.theme.ThemeKt*",
                    "cl.duoc.huerto.presentation.theme.TypeKt*",
                    "cl.duoc.huerto.presentation.theme.SemanticColors*",
                    "cl.duoc.huerto.presentation.navigation.AppNavHostKt*",
                    "cl.duoc.huerto.presentation.navigation.RutasKt*",
                    "cl.duoc.huerto.presentation.navigation.ItemMenu",
                    "cl.duoc.huerto.presentation.screens.panel.ComponentesPanelKt*",
                    "cl.duoc.huerto.presentation.screens.main.*",
                    "cl.duoc.huerto.presentation.screens.avisos.AvisosNotificacionKt*",
                    "cl.duoc.huerto.presentation.screens.sincronizacion.TarjetaSincronizacionKt*",
                    "cl.duoc.huerto.presentation.screens.clima.TarjetaClimaKt*",
                    "cl.duoc.huerto.presentation.screens.tareas.ComponentesTareaKt*",
                    "cl.duoc.huerto.presentation.screens.problemas.ComponentesProblemaKt*",
                    // Fábricas de ViewModel: necesitan Application (Android)
                    "*ViewModel\$Companion*",
                    // Código que genera Room/KSP automáticamente
                    "*_Impl", "*_Impl\$*",
                    // Arranque de Android y configuración
                    "cl.duoc.huerto.MainActivity*",
                    "cl.duoc.huerto.HuertoApp*",
                    "cl.duoc.huerto.BuildConfig",
                    "cl.duoc.huerto.core.di.*",
                    // Base de datos real, datos de ejemplo y cliente HTTP real
                    "cl.duoc.huerto.data.local.AppDatabase*",
                    "cl.duoc.huerto.data.local.DatosDemoKt*",
                    "cl.duoc.huerto.data.remote.RetrofitClient*",
                    "cl.duoc.huerto.data.remote.clima.ClimaClient*",
                    // Archivos (cámara/galería) y notificaciones del sistema
                    "cl.duoc.huerto.core.storage.*",
                    "cl.duoc.huerto.core.notification.*"
                )
            }
        }
        verify {
            rule("Cobertura mínima de líneas del proyecto") {
                minBound(80)
            }
        }
    }
}
