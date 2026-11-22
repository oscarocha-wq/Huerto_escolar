import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp") version "2.3.4"
    // Kover: cobertura de las pruebas unitarias (la versión se define en el build.gradle.kts raíz)
    id("org.jetbrains.kotlinx.kover")
}

// ---------- Firma de la APK release ----------
// Las contraseñas NO van en el repositorio: se leen de keystore.properties (en la raíz,
// ignorado por Git). Si el archivo no existe, el proyecto compila igual (sin firma release).
val archivoFirma = File(rootDir, "keystore.properties")
val datosFirma = Properties().apply {
    if (archivoFirma.exists()) archivoFirma.inputStream().use { load(it) }
}

android {
    namespace = "cl.duoc.huerto"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "cl.duoc.huerto"
        minSdk = 26
        targetSdk = 37
        // versionCode: número entero que Android compara para permitir actualizar (siempre sube).
        // versionName: la versión que ve el usuario.
        versionCode = 2
        versionName = "2.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (archivoFirma.exists()) {
            create("release") {
                storeFile = File(rootDir, datosFirma.getProperty("storeFile"))
                storePassword = datosFirma.getProperty("storePassword")
                keyAlias = datosFirma.getProperty("keyAlias")
                keyPassword = datosFirma.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            if (archivoFirma.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    val roomVersion = "2.6.1"
    implementation(libs.androidx.room.runtime)
    implementation(libs.room.ktx)
    annotationProcessor(libs.room.compiler)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.coil.compose)
    // Room
    implementation("androidx.room:room-runtime:2.8.4")
    implementation("androidx.room:room-ktx:2.8.4")
    ksp("androidx.room:room-compiler:2.8.4")

// Navegación, ViewModel e íconos
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.compose.material:material-icons-extended")

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

    // Retrofit: consumir el backend huertoapi
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
}

// ---------- APK con nombre legible ----------
// gradlew :app:exportarApkRelease  ->  app/build/apk-exportadas/HuertoEscolar-v1.0-build1.apk
val nombreApp = "HuertoEscolar"
val versionApp = android.defaultConfig.versionName ?: "1.0"
val buildApp = android.defaultConfig.versionCode ?: 1

tasks.register<Copy>("exportarApkRelease") {
    dependsOn("assembleRelease")
    from(layout.buildDirectory.dir("outputs/apk/release")) {
        include("app-release.apk")
        rename { "$nombreApp-v$versionApp-build$buildApp.apk" }
    }
    into(layout.buildDirectory.dir("apk-exportadas"))
}

// ---------- Kover (cobertura de pruebas unitarias) ----------
// Informe HTML:  gradlew koverHtmlReportDebug  -> app/build/reports/kover/htmlDebug/index.html
// Informe XML:   gradlew koverXmlReportDebug
// Verificar mínimo (80 %): gradlew koverVerifyDebug
//
// Se mide la lógica que se prueba con pruebas UNITARIAS (domain, data, ViewModels).
// Se excluye lo que solo se puede probar en un dispositivo/emulador:
// pantallas Compose, arranque de Android, Room/Retrofit generados y acceso a archivos/notificaciones.
kover {
    reports {
        filters {
            excludes {
                // Interfaz de Compose (se prueba con pruebas instrumentadas, no unitarias)
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
            rule("Cobertura mínima de líneas") {
                minBound(80)
            }
        }
    }
}
