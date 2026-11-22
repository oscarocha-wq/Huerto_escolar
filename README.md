# 🌱 Huerto Escolar

Aplicación Android para administrar el huerto escolar: sectores, cultivos, tareas de los estudiantes con evidencia fotográfica y validación docente, reporte de problemas, alertas, historial y lecturas de sensores simuladas.

**Asignatura:** DSY1105 – Desarrollo de Aplicaciones Móviles · Duoc UC
**Evaluaciones:** Parcial 4 (encargo) y Parcial 5 (presentación)

## 👥 Integrantes

| Integrante | GitHub |
|---|---|
| Oscar Rocha | [@oscarocha-wq](https://github.com/oscarocha-wq) |
| Yeison Jeldres | [@yeison222](https://github.com/yeison222) |

**Repositorios:** app (este) · microservicio [HuertoBacken](https://github.com/oscarocha-wq/HuertoBacken) (rama `backend`)

**Trello (planificación):** [tablero Huerto Escolar](https://trello.com/invite/b/6ad540853137fdbe6fd9683a/ATTIab341eff750324329f21a33f6fcffa4aB4AA7E14/huerto-escolar-dsy1105)

<!-- Evidencia: agregar captura del tablero con tarjetas asignadas a cada integrante -->
<!-- ![Trello](docs/trello.png) -->

## 🛠️ Tecnologías

- Kotlin + Jetpack Compose (Material 3)
- **Arquitectura limpia** (presentation · domain · data) con **MVVM** en la capa de presentación
- **Room** (KSP) con claves foráneas, índices, transacciones y migraciones v1 → v5
- Navigation Compose con rutas protegidas por perfil
- Photo Picker + cámara (FileProvider) para evidencias
- Notificaciones locales (avisos de riego y tareas)
- **Retrofit + Gson** para consumir el microservicio propio y una API externa
- **Microservicio huertoapi** (Spring Boot 4 + JPA + MySQL)
- Pruebas unitarias con JUnit y kotlinx-coroutines-test (ViewModels, repositorios, dominio)
- Cobertura con **Kover** (mínimo 80 %)
- APK release **firmada** con keystore (`.jks`)

## ✨ Funcionalidades

**Administrador (docente)**
- CRUD de sectores y cultivos (con foto) y reglas de eliminación segura
- Crear y asignar tareas a un estudiante o a un grupo
- Validar o rechazar entregas (motivo obligatorio, mín. 10 caracteres)
- Atender problemas reportados
- Panel con resumen, alertas e historial completo
- Sensores por sector: datos de prueba simulados y registro manual con umbrales

**Estudiante**
- Ver sus tareas, iniciarlas y entregarlas con foto o comentario
- Reenviar tareas rechazadas (se conserva todo el historial)
- Reportar problemas con fotografía

**Ambos perfiles**
- Tarjeta de **clima del huerto** (API externa Open-Meteo) con consejo de riego
- **Sincronizar con el servidor**: trae los datos del microservicio a la app

## 🔗 Endpoints utilizados

### Microservicio propio: huertoapi (`http://10.0.2.2:8080` desde el emulador)

| Método | Endpoint | Uso en la app |
|---|---|---|
| POST | `/usuarios/login` | Iniciar sesión |
| GET | `/usuarios`, `/sectores`, `/cultivos`, `/tareas`, `/problemas` | Botón "Sincronizar con el servidor" |
| POST · PUT · DELETE | `/sectores`, `/sectores/{id}` | Crear, editar y eliminar sectores |
| POST · PUT · DELETE | `/cultivos`, `/cultivos/{id}` | Crear, editar y eliminar cultivos |
| POST · PUT · DELETE | `/tareas`, `/tareas/{id}` | Crear, editar y eliminar tareas |
| PATCH | `/tareas/{id}/estado` | Iniciar, entregar, validar o rechazar tareas |
| POST | `/problemas` | Reportar un problema |
| PATCH | `/problemas/{id}/estado` | Atender un problema (docente) |

Cada cambio se guarda en Room y se envía al microservicio. Si el servidor no responde, el cambio queda guardado en el teléfono y la app sigue funcionando.

### API externa: Open-Meteo (`https://api.open-meteo.com`)

| Método | Endpoint | Uso en la app |
|---|---|---|
| GET | `/v1/forecast?latitude=-33.45&longitude=-70.66&current=temperature_2m,relative_humidity_2m,precipitation,weather_code&daily=precipitation_sum,temperature_2m_max,temperature_2m_min` | Tarjeta "Clima en el huerto" del Inicio |

Es gratuita y no necesita clave. Se consume con un cliente Retrofit separado (`data/remote/clima`) y no se guarda en Room ni en el microservicio.

## 🗂️ Arquitectura y estructura

El proyecto sigue los principios de **arquitectura limpia**: tres capas con dependencias que apuntan siempre hacia el dominio.

```
presentation  ──►  domain  ◄──  data
 (Compose,          (modelos,      (Room, DAOs,
  ViewModels)        reglas,        implementaciones
                     contratos)     de repositorios)
              core/di une las capas
```

- **domain** no depende de ninguna otra capa: contiene los modelos, las reglas de negocio y los *contratos* (interfaces) de los repositorios.
- **data** implementa esos contratos con Room.
- **presentation** solo conoce `domain`: los ViewModels reciben interfaces (`SectorRepository`, `TareaRepository`…) y no saben que existe Room.
- **core/di/AppContainer** es el único punto que crea las implementaciones (inyección de dependencias manual).

```
app/src/main/java/cl/duoc/huerto/
├── MainActivity.kt · HuertoApp.kt
├── core/
│   ├── di/              # AppContainer: inyección de dependencias
│   ├── session/         # SesionManager (usuario conectado)
│   ├── storage/         # Evidencias: copia de fotos al almacenamiento interno
│   └── notification/    # Notificador: canal y avisos del sistema
├── domain/
│   ├── model/           # Entidades, enums, etiquetas, alertas, historial, sensores
│   ├── repository/      # Interfaces (contratos) de los repositorios
│   ├── rules/           # Reglas de negocio: acceso, tareas, problemas, eliminación
│   ├── usecase/         # Cálculos: alertas, avisos, panel/historial, simulación de sensores
│   ├── validation/      # Validadores de formularios y límites de campos
│   └── util/            # Fechas
├── data/
│   ├── local/           # AppDatabase (Room v5 + migraciones), DatosDemo
│   │   └── dao/         # Consultas SQL
│   └── repository/      # *RepositoryImpl: implementaciones de los contratos
└── presentation/
    ├── navigation/      # Rutas y NavHost
    ├── theme/           # Colores, tipografía, tonos de estado
    ├── components/      # Componentes reutilizables (campos, botones, chips, fotos)
    └── screens/         # Una carpeta por módulo: Screen + ViewModel + UiState
        ├── auth/ · main/ · panel/ · sectores/ · sensores/
        └── cultivos/ · tareas/ · problemas/ · avisos/
```

Las pruebas unitarias (`app/src/test`) siguen la misma estructura de paquetes.

Base de datos en 3FN: `usuarios`, `sectores`, `cultivos`, `tareas`, `evidencias`, `validaciones`, `problemas`, `lecturas`.

## 🔑 Usuarios de prueba

| Usuario | Clave | Perfil |
|---|---|---|
| ADM-01 | 1234 | Administrador |
| EST-01 | 1234 | Estudiante (Taller A) |
| EST-02 | 1234 | Estudiante (Taller A) |
| EST-03 | 1234 | Estudiante (Taller B) |

> Los datos de demostración se cargan la primera vez que se crea la base de datos. Si la app ya estaba instalada, desinstálala para verlos.

## ▶️ Cómo ejecutar

1. Clonar el repositorio:
```
   git clone https://github.com/oscarocha-wq/Huerto_escolar.git
```
2. Abrir la carpeta en Android Studio y esperar el *Gradle Sync*.
3. **Build → Rebuild Project**.
4. Ejecutar en un emulador o dispositivo con Android 8.0 (API 26) o superior.

**Microservicio** (para login remoto, sincronización y CRUD contra el servidor):
1. Encender MySQL en XAMPP (usuario `root` sin clave).
2. Clonar [HuertoBacken](https://github.com/oscarocha-wq/HuertoBacken), abrirlo en IntelliJ y ejecutar `HuertoApiApplication` (puerto 8080).
3. Probar en el navegador: `http://localhost:8080/sectores`.
4. Ejecutar la app en el **emulador** (usa `10.0.2.2:8080`).

## 🧪 Pruebas

En Android Studio: clic derecho sobre `app/src/test` → **Run 'Tests in huerto'**, o en la terminal:

```
gradlew test
```

**Cobertura (Kover, mínimo 80 %):**

```
gradlew koverHtmlReport
gradlew koverVerify
```

Informe: `build/reports/kover/html/index.html`. Se excluyen las pantallas de Compose y el código que depende de Android (comentado en `build.gradle.kts`).

<!-- ![Cobertura](docs/cobertura.png) -->

## 📦 APK firmada

| Dato | Valor |
|---|---|
| applicationId | `cl.duoc.huerto` |
| versionName / versionCode | `2.1` / `2` |
| Keystore | `huerto-release.jks` (alias `huerto-release`), en la raíz del repositorio |
| APK | `apk/HuertoEscolar-v2.1-build2.apk` |

Pasos:
1. Copiar `keystore.properties.ejemplo` como `keystore.properties` y completar las contraseñas (este archivo no se sube; las contraseñas se entregan al docente por AVA).
2. Generar: `gradlew :app:exportarApkRelease` → `app/build/apk-exportadas/HuertoEscolar-v2.1-build2.apk`
   (o **Build → Generate Signed App Bundle or APK → APK → release**).
3. Verificar firma: `apksigner verify --verbose --print-certs <apk>`.
4. Instalar: `adb install -r <apk>`.

<!-- Evidencia: captura del APK firmado (Build → Analyze APK) y del archivo .jks -->
<!-- ![APK firmada](docs/apk.png) -->
<!-- ![Keystore](docs/jks.png) -->

