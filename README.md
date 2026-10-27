<h1 align="center">ia</h1>

<p align="center"><b>Claude Code y Codex con modelos gratis, desde un solo comando.</b><br>
Un menú en la terminal para Windows (cmd y PowerShell), macOS, Linux y Android (Termux).</p>

<p align="center">
  <a href="https://github.com/humanx123/ia-puente/stargazers"><img src="https://img.shields.io/github/stars/humanx123/ia-puente?style=flat&color=ff2d2d&label=%E2%98%85%20estrellas" alt="Estrellas"></a>
  <img src="https://img.shields.io/badge/Windows%20%C2%B7%20macOS%20%C2%B7%20Linux%20%C2%B7%20Termux-111" alt="Sistemas">
  <img src="https://img.shields.io/badge/node-%E2%89%A5%2020.18-111" alt="Node 20.18+">
  <a href="LICENSE"><img src="https://img.shields.io/badge/licencia-MIT-111" alt="MIT"></a>
</p>

```
╭─ ia ─ FreeLLMAPI ● en línea ─ Windows ─╮
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

`ia` usa [FreeLLMAPI](https://github.com/tashfeenahmed/freellmapi), un enrutador que reúne decenas de modelos gratuitos (Kilo, OVH, LLM7, Groq, Gemini, OpenRouter, Cerebras, NVIDIA…) detrás de una sola clave. Abre **Claude Code** o **Codex** apuntando a esos modelos sin tocar tu configuración: las credenciales solo se pasan al proceso, y `~/.claude` y `~/.codex` no se modifican.

## Qué hace

- **Instala y enciende FreeLLMAPI solo**: lo descarga, lo compila, crea su clave de cifrado y lo deja corriendo en segundo plano. Cada vez que abres `ia` lo enciende si está apagado.
- **Configura la clave sola**: crea la cuenta del panel o inicia sesión, y lee y guarda tu clave unificada `freellmapi-…`.
- **Activa los proveedores gratis sin clave** (Kilo, OVH, LLM7, AI Horde…) y añade los que piden clave. Muestra el **enlace para conseguir cada una** y lo abre en el navegador.
- **Claude Code o Codex** con FreeLLMAPI o con tu cuenta normal. Si no están instalados, los instala con npm.
- **Un modelo para cada uno**: elige de la lista o deja «automático», que cambia de modelo si uno falla.
- Ve y prueba tus claves, el estado y el registro del servidor; reinícialo o actualízalo.
- Si ya usabas el `ia` de Bash en Termux, **importa su configuración**.

## Instalación

Necesitas **[Node.js 20.18 o más nuevo](https://nodejs.org)** y **git**.

### Windows (cmd o PowerShell)

```bat
winget install OpenJS.NodeJS.LTS
winget install Git.Git
git clone https://github.com/humanx123/ia-puente
cd ia-puente
instalar.cmd
```

Abre una ventana **nueva** de cmd o PowerShell y escribe `ia`. `instalar.cmd` añade la carpeta al PATH de tu usuario. Sin instalar, también funciona con `ia.cmd` desde la carpeta.

### Linux y macOS

```bash
git clone https://github.com/humanx123/ia-puente
cd ia-puente
./instalar.sh
ia
```

`instalar.sh` enlaza `ia` en `/usr/local/bin` o, si no hay permiso, en `~/.local/bin` (y lo añade a tu PATH).

### Android (Termux)

```bash
pkg install nodejs-lts git
git clone https://github.com/humanx123/ia-puente
cd ia-puente && ./instalar.sh
ia
```

En Termux, compilar FreeLLMAPI puede pedir `pkg install python make clang`. Si falla, sigue la [guía de Termux de FreeLLMAPI](https://github.com/tashfeenahmed/freellmapi/blob/main/docs/en/install/02-android-termux.md) o usa Debian (`proot-distro`).

### Con npm (cualquier sistema)

```bash
npm install -g github:humanx123/ia-puente
ia
```

## Primer uso

1. Escribe `ia` y elige **1** (Claude Code) o **2** (Codex).
2. Si FreeLLMAPI no está instalado, `ia` ofrece instalarlo. Tarda unos minutos la primera vez.
3. Crea la cuenta del panel de FreeLLMAPI (email y contraseña, solo en tu equipo). `ia` guarda la clave y activa los proveedores gratis.
4. Opcional: en **8 → Proveedores**, añade claves gratuitas para tener más modelos y más rápidos. Recomendadas: **Groq**, **Gemini (Google AI Studio)**, **OpenRouter** y **Cerebras**. Cada una trae su enlace.

¿Ya tienes FreeLLMAPI (por ejemplo la app de escritorio, que usa el puerto 3001)? En **5 → Configuración → URL**, escribe `http://127.0.0.1:3001`.

## Comandos

| Comando | Qué hace |
|---|---|
| `ia` | Menú interactivo |
| `ia claude` | Claude Code con FreeLLMAPI |
| `ia codex` | Codex con FreeLLMAPI |
| `ia estado` | Estado, clave, modelos e instalación |
| `ia instalar` | Instala o actualiza FreeLLMAPI en este equipo |
| `ia encender` / `ia apagar` | Enciende o apaga FreeLLMAPI en segundo plano |
| `ia proveedores` | Proveedores gratis y claves API |
| `ia donar` | Direcciones para donar, con código QR en la terminal |
| `ia ayuda` · `ia version` | Ayuda y versión |

## Dónde guarda las cosas

| | Windows | macOS | Linux / Termux |
|---|---|---|---|
| Configuración | `%APPDATA%\ia\config.json` | `~/.config/ia/config.json` | `~/.config/ia/config.json` |
| FreeLLMAPI | `%LOCALAPPDATA%\ia\freellmapi` | `~/Library/Application Support/ia/freellmapi` | `~/.local/share/ia/freellmapi` |

La configuración guarda tu clave unificada y la sesión del panel, y solo la puede leer tu usuario. Tus claves de proveedores se quedan cifradas dentro de FreeLLMAPI.

## Proyectos hermanos

- **[laboratorio.ia](https://github.com/humanx123/lab-ia)**: chat, investigación científica verificada e imágenes con IA, en tu Cloudflare o en tu teléfono. [Pruébalo sin registrarte](https://lab-ia-demo.reserchlab.workers.dev).
- **[FreeLLMAPI](https://github.com/tashfeenahmed/freellmapi)**: el enrutador de modelos gratuitos que usa `ia` (proyecto independiente, licencia MIT).

## Apoya el proyecto

Si `ia` te sirve, dale una **★ estrella** y compártelo. También puedes donar en cripto (`ia donar` muestra los QR en la terminal):

| Bitcoin (BTC) | Monero (XMR) | USDC (red Arbitrum One) |
|:---:|:---:|:---:|
| <img src="docs/btc.svg" width="170" alt="QR Bitcoin"> | <img src="docs/xmr.svg" width="170" alt="QR Monero"> | <img src="docs/usdc.svg" width="170" alt="QR USDC"> |
| `bc1qd9j45f4t0jwyhjhqh2kvz2cr7k8xye460rr2y7` | `447gTj6Hg6gaAEAUmjqfhqDZr1PziUTvbT4LYLpmLVnTNVFK6cqeqPfh6P4neMKLWX5jDXAr94fWHacJwDvjmCzBBH8wPBt` | `0xAC43f9b0E7F0ae6386cfC8ef6BFf62CE9c3006A1` (mínimo 5 USDC) |

Envía USDC solo por la red **Arbitrum One**. Por otra red, los fondos se pierden.

¡Gracias!<br>
<sub>Hecho xhumanx & IA</sub>

## Licencia

[MIT](LICENSE). Incluye el generador de QR de Kazuhiko Arase (MIT). Claude Code, Codex y FreeLLMAPI son proyectos de sus autores; `ia` solo los abre.
