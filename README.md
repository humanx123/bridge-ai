<h1 align="center">🌉 bridge-ai</h1>

<p align="center"><b>Claude Code y Codex con modelos gratis, desde un solo comando.</b><br>
Un menú en la terminal para Windows (cmd y PowerShell), macOS, Linux y Android (Termux).</p>

<p align="center"><i>bridge-ai: un puente en la terminal que conecta Claude Code y Codex con modelos gratuitos.<br>Una herramienta neutral y objetiva para interactuar con IA.</i></p>

<p align="center">
  <a href="https://github.com/humanx123/bridge-ai/stargazers"><img src="https://img.shields.io/github/stars/humanx123/bridge-ai?style=flat&color=ff2d2d&label=%E2%98%85%20estrellas" alt="Estrellas"></a>
  <img src="https://img.shields.io/badge/Windows%20%C2%B7%20macOS%20%C2%B7%20Linux%20%C2%B7%20Termux-111" alt="Sistemas">
  <img src="https://img.shields.io/badge/node-%E2%89%A5%2020.18-111" alt="Node 20.18+">
  <a href="LICENSE"><img src="https://img.shields.io/badge/licencia-MIT-111" alt="MIT"></a>
</p>

```
╭─ bridge-ai ─ FreeLLMAPI ● en línea ─ Windows ─╮
  1) Claude Code  (FreeLLMAPI, modelos gratis)
  2) Codex        (FreeLLMAPI, modelos gratis)
  3) Claude Code  (tu cuenta de Anthropic)
  4) Codex        (tu cuenta de OpenAI)
  5) Configuración (URL, clave, modelos, carpeta, instalar)
  6) Estado
  7) Abrir una terminal aquí
  8) Proveedores y claves API (gratis, añadir claves)
  9) laboratorio.ia (chat e investigación científica)
  D) Donar · acerca de
  0) Salir
```

`bridge-ai` usa [FreeLLMAPI](https://github.com/tashfeenahmed/freellmapi), un enrutador que reúne decenas de modelos gratuitos (Kilo, OVH, LLM7, Groq, Gemini, OpenRouter, Cerebras, NVIDIA…) detrás de una sola clave. Abre **Claude Code** o **Codex** apuntando a esos modelos sin tocar tu configuración: las credenciales solo se pasan al proceso, y `~/.claude` y `~/.codex` no se modifican.

## Qué hace

- **Instala y enciende FreeLLMAPI solo**: lo descarga, lo compila, crea su clave de cifrado y lo deja corriendo en segundo plano. Cada vez que abres `bridge-ai` lo enciende si está apagado.
- **Configura la clave sola**: crea la cuenta del panel o inicia sesión, y lee y guarda tu clave unificada `freellmapi-…`.
- **Activa los proveedores gratis sin clave** (Kilo, OVH, LLM7, AI Horde…) y añade los que piden clave. Muestra el **enlace para conseguir cada una** y lo abre en el navegador.
- **Claude Code o Codex** con FreeLLMAPI o con tu cuenta normal. Si no están instalados, los instala con npm.
- **Un modelo para cada uno**: elige de la lista o deja «automático», que cambia de modelo si uno falla.
- Ve y prueba tus claves, el estado y el registro del servidor; reinícialo o actualízalo.
- Si ya usabas el antiguo `ia` de Bash en Termux, **importa su configuración**.

## Instalación

Necesitas **[Node.js 20.18 o más nuevo](https://nodejs.org)** y **git**. Las instrucciones de abajo son paso a paso y asumen una instalación desde cero.

### Windows (cmd o PowerShell)

1. Instala Node.js LTS y git. En una terminal como administrador:
   ```bat
   winget install OpenJS.NodeJS.LTS
   winget install Git.Git
   ```
2. Abre una terminal **nueva** (para que el PATH tome Node.js y git) y clona el repo:
   ```bat
   git clone https://github.com/humanx123/bridge-ai
   cd bridge-ai
   ```
3. Ejecuta el instalador (doble clic o desde cmd):
   ```bat
   instalar.cmd
   ```
   Añade la carpeta del repo al PATH de tu usuario.
4. Abre una ventana **nueva** de cmd o PowerShell y escribe:
   ```bat
   bridge-ai
   ```
   Sin instalador, también funciona con `bridge-ai.cmd` desde la carpeta del repo.

### Linux y macOS

1. Instala Node.js 20.18+ y git con tu gestor (`apt`, `dnf`, `brew`, etc.). En Debian/Ubuntu:
   ```bash
   sudo apt update && sudo apt install -y nodejs npm git
   node -v   # debe ser >= v20.18
   ```
   Si tu distro trae Node viejo, usa [NodeSource](https://nodejs.org/en/download/package-manager) o [nvm](https://github.com/nvm-sh/nvm).
2. Clona e instala:
   ```bash
   git clone https://github.com/humanx123/bridge-ai
   cd bridge-ai
   ./instalar.sh
   ```
   El script enlaza `bridge-ai` (y el atajo `ia`) en `/usr/local/bin` si tienes permiso, o en `~/.local/bin` (y lo agrega al PATH de `~/.bashrc`, `~/.zshrc` o `~/.profile`).
3. Abre una terminal **nueva** (o `source` tu rc) y arranca:
   ```bash
   bridge-ai
   ```

### Android (Termux nativo, sin PRoot)

Probado en Termux de Google Play 2026.10.09 sobre Android 15 aarch64 con Node.js 24.17.0 y npm 11.17.0.

1. Instala desde Termux recién abierto:
   ```bash
   pkg update && pkg upgrade -y
   pkg install -y nodejs-lts git
   ```
   Si prefieres Node más reciente: `pkg install -y nodejs`.
2. Clona e instala bridge-ai:
   ```bash
   git clone https://github.com/humanx123/bridge-ai
   cd bridge-ai
   ./instalar.sh
   ```
   El instalador detecta `$PREFIX/bin` de Termux y enlaza ahí.
3. Compiladores para FreeLLMAPI (la primera vez que elijas instalarlo desde el menú):
   ```bash
   pkg install -y python make clang
   ```
4. Arranca:
   ```bash
   bridge-ai
   ```

**Notas de compatibilidad en Termux (importantes):**

- **Claude Code**: las versiones recientes requieren binarios nativos de Linux que no son compatibles con Android/Bionic (`claude native binary not installed`). La versión **2.1.112** conserva la implementación en JavaScript puro y funciona:
  ```bash
  npm install -g @anthropic-ai/claude-code@2.1.112
  claude --version
  ```
  Evita actualizar a versiones más nuevas si estás en Termux nativo.
- **Codex**: el paquete oficial `@openai/codex` no provee binarios para `linux-arm64` en Termux (falla con `@openai/codex-linux-arm64` no encontrado). La alternativa comunitaria que sí funciona en ARM64 es:
  ```bash
  npm install -g @mmmbuto/codex-cli-termux
  codex --version
  ```
  Esto **no es un paquete oficial de OpenAI**; úsalo bajo tu propio criterio.
- Si prefieres un entorno Linux completo, instala Debian con PRoot y usa las instrucciones de Linux de arriba:
  ```bash
  pkg install -y proot-distro
  proot-distro install debian
  proot-distro login debian
  ```

### Con npm (cualquier sistema)

```bash
npm install -g github:humanx123/bridge-ai
bridge-ai
```

Expone los comandos `bridge-ai` e `ia` globalmente sin tocar el PATH a mano.

### Verificar la instalación

```bash
bridge-ai version    # imprime la versión del paquete
bridge-ai estado     # muestra sistema, FreeLLMAPI y agentes detectados
```

## Primer uso

1. Escribe `bridge-ai` y elige **1** (Claude Code) o **2** (Codex).
2. Si FreeLLMAPI no está instalado, `bridge-ai` ofrece instalarlo. Tarda unos minutos la primera vez.
3. Crea la cuenta del panel de FreeLLMAPI (email y contraseña, solo en tu equipo). `bridge-ai` guarda la clave y activa los proveedores gratis.
4. Opcional: en **8 → Proveedores**, añade claves gratuitas para tener más modelos y más rápidos. Recomendadas: **Groq**, **Gemini (Google AI Studio)**, **OpenRouter** y **Cerebras**. Cada una trae su enlace.

¿Ya tienes FreeLLMAPI (por ejemplo la app de escritorio, que usa el puerto 3001)? En **5 → Configuración → URL**, escribe `http://127.0.0.1:3001`.

## Comandos

| Comando | Qué hace |
|---|---|
| `bridge-ai` (o solo `ia`) | Menú interactivo |
| `bridge-ai claude` | Claude Code con FreeLLMAPI |
| `bridge-ai codex` | Codex con FreeLLMAPI |
| `bridge-ai estado` | Estado, clave, modelos e instalación |
| `bridge-ai instalar` | Instala o actualiza FreeLLMAPI en este equipo |
| `bridge-ai encender` / `bridge-ai apagar` | Enciende o apaga FreeLLMAPI en segundo plano |
| `bridge-ai proveedores` | Proveedores gratis y claves API |
| `bridge-ai donar` | Direcciones para donar, con código QR en la terminal |
| `bridge-ai ayuda` · `bridge-ai version` | Ayuda y versión |

## Dónde guarda las cosas

| | Windows | macOS | Linux / Termux |
|---|---|---|---|
| Configuración | `%APPDATA%\bridge-ai\config.json` | `~/.config/bridge-ai/config.json` | `~/.config/bridge-ai/config.json` |
| FreeLLMAPI | `%LOCALAPPDATA%\bridge-ai\freellmapi` | `~/Library/Application Support/bridge-ai/freellmapi` | `~/.local/share/bridge-ai/freellmapi` |

La configuración guarda tu clave unificada y la sesión del panel, y solo la puede leer tu usuario. Tus claves de proveedores se quedan cifradas dentro de FreeLLMAPI.

## Proyectos hermanos

- **[laboratorio.ia](https://github.com/ReserchLab/lab-ia)**: chat, investigación científica verificada e imágenes con IA, en tu Cloudflare o en tu teléfono. [Pruébalo sin registrarte](https://lab-ia-demo.reserchlab.workers.dev).
- **[FreeLLMAPI](https://github.com/tashfeenahmed/freellmapi)**: el enrutador de modelos gratuitos que usa `bridge-ai` (proyecto independiente, licencia MIT).

## Apoya el proyecto

Si `bridge-ai` te sirve, dale una **★ estrella** y compártelo. También puedes donar en cripto (`bridge-ai donar` muestra los QR en la terminal):

| Bitcoin (BTC) | Monero (XMR) | USDC (red Arbitrum One) |
|:---:|:---:|:---:|
| <img src="docs/btc.svg" width="170" alt="QR Bitcoin"> | <img src="docs/xmr.svg" width="170" alt="QR Monero"> | <img src="docs/usdc.svg" width="170" alt="QR USDC"> |
| `bc1qd9j45f4t0jwyhjhqh2kvz2cr7k8xye460rr2y7` | `447gTj6Hg6gaAEAUmjqfhqDZr1PziUTvbT4LYLpmLVnTNVFK6cqeqPfh6P4neMKLWX5jDXAr94fWHacJwDvjmCzBBH8wPBt` | `0xAC43f9b0E7F0ae6386cfC8ef6BFf62CE9c3006A1` (mínimo 5 USDC) |

Envía USDC solo por la red **Arbitrum One**. Por otra red, los fondos se pierden.

¡Gracias!<br>
<sub>Hecho xhumanx & IA</sub>

## Licencia

[MIT](LICENSE). Incluye el generador de QR de Kazuhiko Arase (MIT). Claude Code, Codex y FreeLLMAPI son proyectos de sus autores; `bridge-ai` solo los abre.
