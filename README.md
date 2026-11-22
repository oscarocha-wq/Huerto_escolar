# 🌱 Huerto Escolar

Aplicación Android para administrar el huerto escolar: sectores, cultivos, tareas de los estudiantes con evidencia fotográfica y validación docente, reporte de problemas, alertas, historial y lecturas de sensores simuladas.

**Asignatura:** DSY1105 – Desarrollo de Aplicaciones Móviles · Duoc UC
**Evaluación:** Parcial 2

## 👥 Integrantes

| Integrante | GitHub |
|---|---|
| Oscar Rocha | [@oscarocha-wq](https://github.com/oscarocha-wq) |
| Yeison Jeldres | — |

Link de trello: https://trello.com/invite/b/6ad540853137fdbe6fd9683a/ATTIab341eff750324329f21a33f6fcffa4aB4AA7E14/huerto-escolar-dsy1105
## 🛠️ Tecnologías

- Kotlin + Jetpack Compose (Material 3)
- **Arquitectura limpia** (presentation · domain · data) con **MVVM** en la capa de presentación
- **Room** (KSP) con claves foráneas, índices, transacciones y migraciones v1 → v5
- Navigation Compose con rutas protegidas por perfil
- Photo Picker + cámara (FileProvider) para evidencias
- Notificaciones locales (avisos de riego y tareas)
- Pruebas unitarias con JUnit (reglas de negocio en `domain/`, ViewModel de login)

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

## 🧪 Pruebas

En Android Studio: clic derecho sobre `app/src/test` → **Run 'Tests in huerto'**, o en la terminal:

```
gradlew test
```

cobertura:
.\gradlew koverHtmlReport

