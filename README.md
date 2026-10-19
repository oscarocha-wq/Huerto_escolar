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
- Arquitectura **MVVM**: Pantalla → ViewModel (StateFlow) → Repositorio → Room DAO
- **Room** (KSP) con claves foráneas, índices, transacciones y migraciones v1 → v5
- Navigation Compose con rutas protegidas por perfil
- Photo Picker + cámara (FileProvider) para evidencias
- Notificaciones locales (avisos de riego y tareas)
- Pruebas unitarias con JUnit (reglas de negocio en `domain/`)

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

## 🗂️ Estructura

```
app/src/main/java/cl/duoc/huerto/
├── data/          # Room: entidades, DAOs, repositorios, base de datos, datos demo
├── domain/        # Reglas de negocio y validaciones (funciones puras, con tests)
├── navigation/    # Rutas y menú por perfil
├── u_i/           # Pantallas y ViewModels por módulo
└── ui/theme/      # Colores, tipografía, colores semánticos y tonos de estado
```

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
