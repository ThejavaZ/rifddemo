# RFID Platform

Plataforma de inventario con RFID: una **app Android** (Kotlin + Jetpack Compose) captura lecturas de tags y las sincroniza contra una **API .NET 8** con **arquitectura hexagonal** persistiendo en **PostgreSQL**.

```text
┌──────────────────────── app Android ───────────────────────┐
│  Zebra RFID SDK / Mock ──► Reader ──► Room (cola PENDING)   │
│  CameraX + ML Kit ──► Barcode        WorkManager (sync)    │
│                                    │  Ktor + JWT Bearer     │
└────────────────────────────────────┼───────────────────────┘
                                     ▼
┌────────────────────── API .NET 8 (hexagonal) ──────────────┐
│  Controllers ──► Casos de uso ──► Puertos ──► Adaptadores   │
│  (entrada)      (Application)    (Domain)    (EF Core/BCrypt│
│                                                      /JWT) │
└────────────────────────────────────┬───────────────────────┘
                                     ▼
                              PostgreSQL 18
```

## Stack

| App Android (`android/`) | Backend (`backend/`) |
|---|---|
| Kotlin 2.2.10 · Jetpack Compose (BOM 2026.02.01) | .NET 8 · ASP.NET Core Web API |
| MVVM + Hilt 2.60.1 + Navigation Compose | Hexagonal: Domain / Application / Infrastructure / Api |
| Room 2.7.0 + WorkManager 2.10.1 | EF Core 8.0.31 + Npgsql (migraciones versionadas) |
| Ktor 3.3.1 (client) | JWT Bearer + BCrypt (cost 11) |
| CameraX 1.6.2 + ML Kit 17.3.0 | Docker Compose (API + PostgreSQL 18) |
| Zebra RFID SDK (o Mock) | `dotnet-ef` (tool en `.config/dotnet-tools.json`) |
| ktlint 14.2.0 · detekt 1.23.8 · JUnit5/MockK/Turbine | — |

## Estructura del repositorio

```text
rifddemo/
├── android/
│   └── app/src/main/java/dev/javiersg/rfiddemo/
│       ├── core/          # navigation, theme, logging (DiagnosticLogger)
│       ├── data/          # api (Ktor), dao, database (Room), repository, rfid, sync
│       ├── domain/        # model, repository (puertos), usecase — puro Kotlin
│       ├── features/      # login, reader, inventory, barcode, home, settings
│       ├── di/            # módulos Hilt
│       └── MainActivity.kt · RfidApplication.kt
└── backend/
    ├── RfidPlatform.sln · global.json (SDK 8.0.425)
    ├── Dockerfile · docker-compose.yml
    ├── RFID.Domain/          # entidades + puertos (cero dependencias)
    ├── RFID.Application/     # casos de uso + puertos de aplicación
    ├── RFID.Infrastructure/  # EF Core, BCrypt, JWT, seeders, migraciones
    └── RFID.Api/             # Controllers, middleware, Program.cs
```

---

## Backend

### Arquitectura hexagonal

Cuatro proyectos con dependencias **hacia el centro**:

```text
RFID.Api ──────────► RFID.Application ──► RFID.Domain ◄── RFID.Infrastructure
(adaptadores            (casos de uso,       (entidades,      (EF Core, BCrypt,
 de entrada:            puertos de           puertos:         JWT, seeders —
 Controllers)           aplicación)          I*Repository)     adaptadores)
```

- **Domain** no referencia EF, HTTP ni ninguna infraestructura: define `Tag`, `InventoryItem`, `User` y los puertos `ITagRepository`/`IInventoryRepository`/`IUserRepository`.
- **Application** contiene los casos de uso (`Login`, `SyncTags`, `GetInventoryByEpc`) y los puertos `ITokenService`/`IPasswordHasher`.
- **Infrastructure** implementa los puertos con EF Core + PostgreSQL (convención snake_case: mismas tablas `tags`/`inventory`/`users`), BCrypt y JWT; además ejecuta migraciones y seeders al arrancar.
- **Api** expone los Controllers (adaptadores de entrada) y traduce `ValidationException` → `400 {"error": ...}`.

### Arranque rápido (un solo comando)

```bash
cd backend
docker compose up --build
```

Al arrancar: PostgreSQL pasa el healthcheck → la API aplica las migraciones pendientes (`InitialCreate`) → corren los seeders idempotentes (`admin` + productos demo). El volumen `rfid_pgdata` conserva la data entre arranques.

| Servicio | Puerto (host) | Notas |
|---|---|---|
| API | **5000** | `5000:8080` (contenedor escucha en 8080) |
| PostgreSQL | 5434 | Solo para `psql` desde el host; la API usa `db:5432` |

Comandos útiles:

```bash
docker compose up -d          # arrancar en background
docker compose logs -f api    # ver logs
docker compose down           # parar (conserva datos)
docker compose down -v        # parar y BORRAR la BD
```

### Endpoints

| Método | Ruta | Auth | Descripción |
|---|---|---|---|
| POST | `/api/v1/auth/token` | — | Login → `{ token, expiresAt }` (JWT, 1 h) |
| POST | `/api/v1/sync/tags` | Bearer | Upsert idempotente de un lote (máx. 500) → `{ syncedCount }` |
| GET | `/api/v1/inventory/{epc}` | Bearer | Item por EPC → `{ id, epc, name, quantity }` o 404 |

```bash
# Login
curl -X POST http://localhost:5000/api/v1/auth/token \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123"}'

# Sync (con el token anterior)
curl -X POST http://localhost:5000/api/v1/sync/tags \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{"tags":[{"epc":"E280116000000209","rssi":-50,"antenna":1,"readCount":3,"lastSeenTimestamp":1770000000000}]}'
```

Errores: credenciales inválidas → `401`; validaciones de negocio → `400 {"error":"..."}`.

### Migraciones y seeders

```bash
cd backend
dotnet tool restore
dotnet ef migrations add <Nombre> --project RFID.Infrastructure --startup-project RFID.Api
```

Las migraciones se aplican solas al arrancar (`DatabaseInitializer`: `Migrate()` + seeders). Los seeders (`InventorySeeder`, `UserSeeder`) son idempotentes: solo insertan si los datos no existen.

### Ejecución sin Docker

```bash
cd backend
ASPNETCORE_URLS="http://0.0.0.0:5000" ASPNETCORE_ENVIRONMENT=Development \
  dotnet run --project RFID.Api --no-launch-profile
```

Requiere un PostgreSQL local con la BD `rfid_api` (ver `ConnectionStrings:Database` en `appsettings.json`).

### Configuración (`RFID.Api/`)

| Clave | Valor (dev) | Uso |
|---|---|---|
| `Auth:SigningKey` | `appsettings.Development.json` | Firma/validación JWT |
| `Auth:SeedUser:Username/Password` | `admin` / `123` | Semilla del usuario inicial |
| `ConnectionStrings:Database` | `localhost:5432`, user `rfid` | En Docker la pisa la variable de entorno del compose |

---

## App Android

### Ejecutar

1. Abrir `android/` en Android Studio (AGP 9.2.1, Gradle 9.4.1).
2. **Dispositivo físico y PC en la misma red WiFi.**
3. `app/build.gradle.kts` → `BuildConfig.BASE_URL` con la IP de tu PC: `http://<IP>:5000` (por defecto `192.168.100.8:5000`).
4. Run ▶ — login con **`admin` / `123`**.

| Flag (`defaultConfig`) | Efecto |
|---|---|
| `USE_MOCK_READER = true` | `MockRfidReader` (desarrollo sin hardware) |
| `USE_MOCK_READER = false` | `ZebraRfidReader` (SDK Zebra real) |

### Funcionalidades

| Feature | Pantalla | Descripción |
|---|---|---|
| `login` | LoginScreen | JWT → `TokenStore` (EncryptedSharedPreferences) |
| `reader` | ReaderScreen | Lectura de tags, estado en vivo, háptico/acústico |
| `inventory` | InventoryScreen | Consulta de inventario por EPC vía API |
| `barcode` | BarcodeScannerScreen | Cámara + ML Kit (Code128/39, QR, DataMatrix, EAN/UPC) |
| `home` / `settings` | — | Navegación y diagnóstico (`DiagnosticLogger`, buffer 200 eventos) |

Cola offline: lecturas → Room (`PENDING`) → `SyncTagsWorker` (WorkManager, único trabajo) → API (`PENDING → SYNCING → SYNCED/FAILED`, reintentos máx. 5).

### Tests y calidad

```bash
cd android
./gradlew :app:testDebugUnitTest    # 20 tests (JUnit5 + MockK + Turbine)
./gradlew :app:ktlintCheck          # estilo
./gradlew :app:detekt               # análisis estático
./gradlew :app:assembleDebug        # build
```

Backend: `dotnet build backend/RfidPlatform.sln` (0 warnings).

---

## Seguridad

- **Auth**: JWT de 1 h firmado con clave simétrica; contraseñas con **BCrypt** (nunca en claro, ni en la BD ni en logs).
- **En desarrollo** la clave JWT vive en `appsettings.Development.json` y el usuario semilla en `Auth:SeedUser`. **En producción**: inyectar `Auth__SigningKey` y `Auth__SeedUser__*` por variables de entorno, desactivar el seed y terminar el tráfico con **HTTPS** (los contenedores exponen HTTP).
- La app Android usa `usesCleartextTraffic` solo porque la demo va por HTTP en LAN.

## Estado

Fases 1–3 completadas (días 1–15): dominio y lector mock → Room + sync + backend → Hilt, SDK Zebra, CameraX/ML Kit, JWT, navegación y calidad de código. Etapa actual: arquitectura hexagonal + EF Core + Docker.
