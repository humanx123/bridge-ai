# bridge-ai · Android

APK cyberpunk que habla con tu FreeLLMAPI local. Estética monocromática azul,
monospace, bordes ASCII — supera a `horus` siendo específica de FreeLLMAPI y
multilingüe (es/en), sin binarios propietarios, apta para F-Droid.

## Arquitectura

- **Kotlin + Jetpack Compose + Material3**, tema oscuro propio.
- **Ktor + OkHttp** para HTTP con streaming SSE contra `/v1/chat/completions`.
- **DataStore Preferences** para URL, API key y modelo por defecto.
- **Sin blobs nativos** (usa `FontFamily.Monospace` del sistema); APK < 6 MB.
- **minSdk 24** (Android 7.0), **targetSdk 34**.
- Cleartext permitido **solo** a `127.0.0.1`, `localhost`, `10.0.2.2` (emulador).

```
android/
├── app/src/main/
│   ├── AndroidManifest.xml
│   ├── kotlin/com/bridgeai/android/
│   │   ├── BridgeAiApp.kt          # Application + CoroutineScope
│   │   ├── MainActivity.kt         # host Compose
│   │   ├── data/
│   │   │   ├── Settings.kt         # DataStore
│   │   │   └── Providers.kt        # catálogo de proveedores
│   │   ├── net/
│   │   │   ├── FreeLlmApiClient.kt # ping / models / chat / chatStream
│   │   │   └── Models.kt           # DTOs serializables
│   │   └── ui/
│   │       ├── Navigation.kt
│   │       ├── theme/              # Color / Type / Theme
│   │       ├── components/         # CyberBox, CyberMenuButton…
│   │       └── screens/            # Home / Chat / Config / Providers / Status / About
│   └── res/                        # strings, colors, icono adaptativo, network config
├── fastlane/metadata/android/      # metadata F-Droid (en-US, es-ES)
└── build.gradle.kts · settings.gradle.kts
```

## Qué resuelve que `horus` no

| | horus | bridge-ai |
|---|---|---|
| Endpoint | genérico OpenAI-compatible | específico FreeLLMAPI (ping, panel, modelos) |
| Gestión de claves | manual | catálogo con enlace «obtener clave» por proveedor |
| Idiomas | inglés | es + en |
| Streaming | polling | SSE real sobre `/v1/chat/completions` |
| Icono | genérico | adaptativo + monocromo (tema del sistema) |
| F-Droid | parcial | metadata fastlane completa, sin binarios |

## Compilar

Dos vías:

**1. GitHub Actions** (recomendado — construye solo al hacer push):
```
.github/workflows/android.yml  →  artefactos: bridge-ai-debug-apk, bridge-ai-release-unsigned-apk
```

**2. Local** (requiere JDK 17 y Android SDK con `platforms;android-34` + `build-tools;34.0.0`):
```bash
cd android
gradle wrapper --gradle-version 8.9
./gradlew :app:assembleDebug    # app/build/outputs/apk/debug/app-debug.apk
./gradlew :app:assembleRelease  # app/build/outputs/apk/release/app-release-unsigned.apk
```

Firma del release: configura `KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`,
`KEY_PASSWORD` en tu entorno (o en secretos de GitHub) y añade `signingConfigs`
al bloque `android { }` de `app/build.gradle.kts` antes de subir a F-Droid.

## Publicar en F-Droid

1. `fastlane/metadata/android/{en-US,es-ES}/*` ya está listo.
2. Abre PR a `fdroid/fdroiddata` con una receta `metadata/com.bridgeai.android.yml`:
   ```yaml
   Categories: [Internet]
   License: MIT
   SourceCode: https://github.com/humanx123/bridge-ai
   IssueTracker: https://github.com/humanx123/bridge-ai/issues
   AutoName: bridge-ai

   RepoType: git
   Repo: https://github.com/humanx123/bridge-ai

   Builds:
     - versionName: 0.1.0
       versionCode: 1
       commit: v0.1.0-android
       subdir: android/app
       gradle:
         - yes

   AutoUpdateMode: Version v%v-android
   UpdateCheckMode: Tags
   CurrentVersion: 0.1.0
   CurrentVersionCode: 1
   ```
3. F-Droid compila desde la fuente y firma con su propio keystore.

## Primer uso

1. Instala y abre `bridge-ai`.
2. Ve a **[3] Configuración** y escribe la URL de tu FreeLLMAPI:
   - Mismo dispositivo (Termux): `http://127.0.0.1:31415`
   - App de escritorio local: `http://127.0.0.1:3001`
   - LAN: `http://<ip>:31415`
3. Pega tu clave unificada `freellmapi-…` (menú **5 → clave** del CLI).
4. **[1] Chat** — escribe, el modelo en `auto` deja que el gateway elija.

## Licencia

MIT. Mismo paquete que el CLI, código separado bajo `/android`.
