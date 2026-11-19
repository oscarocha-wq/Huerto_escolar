plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp") version "2.3.4"
    // Kover: cobertura de las pruebas unitarias
    id("org.jetbrains.kotlinx.kover") version "0.9.11"
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
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
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

// ---------- Kover (cobertura de pruebas unitarias) ----------
// Informe HTML:  gradlew koverHtmlReportDebug  -> app/build/reports/kover/htmlDebug/index.html
// Informe XML:   gradlew koverXmlReportDebug
// Verificar mínimo: gradlew koverVerifyDebug
kover {
    reports {
        filters {
            excludes {
                // Pantallas de Compose: se prueban con pruebas de interfaz, no unitarias
                annotatedBy("androidx.compose.runtime.Composable")
                classes(
                    "*ComposableSingletons*",
                    // Código que genera Room/KSP automáticamente
                    "*_Impl", "*_Impl\$*",
                    // Arranque de Android y configuración (sin lógica que probar)
                    "cl.duoc.huerto.MainActivity",
                    "cl.duoc.huerto.HuertoApp",
                    "cl.duoc.huerto.BuildConfig",
                    "cl.duoc.huerto.data.local.AppDatabase*",
                    "cl.duoc.huerto.data.remote.RetrofitClient*"
                )
            }
        }
        verify {
            rule("Cobertura mínima de líneas") {
                // Ajusten este número al porcentaje real que les dé el informe
                minBound(30)
            }
        }
    }
}
