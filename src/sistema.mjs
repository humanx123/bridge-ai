// Lo que cambia entre Windows, macOS, Linux y Termux: carpetas, abrir enlaces, buscar y ejecutar programas.
import { spawn, spawnSync } from 'node:child_process';
import { existsSync } from 'node:fs';
import { homedir } from 'node:os';
import { join } from 'node:path';
import { soltarEntrada } from './ui.mjs';

export const WIN = process.platform === 'win32';
export const MAC = process.platform === 'darwin';
const TERMUX_BIN = '/data/data/com.termux/files/usr/bin';
// Termux directo o Debian dentro de Termux (proot): en los dos se abren enlaces con termux-open-url.
export const TERMUX = Boolean(process.env.TERMUX_VERSION) || existsSync(join(TERMUX_BIN, 'termux-open-url'));

export const NOMBRE_SISTEMA = WIN ? 'Windows' : MAC ? 'macOS' : TERMUX ? 'Android (Termux)' : 'Linux';

/** Configuración: %APPDATA%\guarura en Windows, ~/.config/guarura en el resto. */
export function carpetaConfig() {
  if (WIN) return join(process.env.APPDATA || join(homedir(), 'AppData', 'Roaming'), 'guarura');
  return join(process.env.XDG_CONFIG_HOME || join(homedir(), '.config'), 'guarura');
}

/** Datos (FreeLLMAPI instalado por Guarura): %LOCALAPPDATA%\guarura, ~/Library/Application Support/guarura o ~/.local/share/guarura. */
export function carpetaDatos() {
  if (WIN) return join(process.env.LOCALAPPDATA || join(homedir(), 'AppData', 'Local'), 'guarura');
  if (MAC) return join(homedir(), 'Library', 'Application Support', 'guarura');
  return join(process.env.XDG_DATA_HOME || join(homedir(), '.local', 'share'), 'guarura');
}

/** ¿Está este programa en el PATH? */
export function existe(programa) {
  const r = WIN
    ? spawnSync('where', [programa], { stdio: 'ignore' })
    : spawnSync('sh', ['-c', `command -v "${programa}"`], { stdio: 'ignore' });
  return r.status === 0;
}

/** En Windows los programas de npm son .cmd: hace falta el shell, y entonces hay que citar los argumentos. */
const citar = a => (/[\s"&|<>^()]/.test(a) ? `"${String(a).replace(/"/g, '""')}"` : a);

/** Ejecuta un programa en esta misma terminal (Claude Code, Codex, npm…) y devuelve su código de salida. */
export function ejecutar(programa, args = [], opciones = {}) {
  soltarEntrada();
  return new Promise(resolve => {
    const hijo = spawn(WIN ? citar(programa) : programa, WIN ? args.map(citar) : args, { stdio: 'inherit', shell: WIN, ...opciones });
    hijo.on('error', e => { console.error(`No se pudo ejecutar ${programa}: ${e.message}`); resolve(127); });
    hijo.on('exit', codigo => resolve(codigo ?? 1));
  });
}

/** Abre un enlace en el navegador del sistema. Devuelve false si no hay cómo. */
export function abrirEnlace(url) {
  const intentos = WIN ? [['cmd', ['/c', 'start', '""', url.replace(/&/g, '^&')]]]
    : MAC ? [['open', [url]]]
    : [[join(TERMUX_BIN, 'termux-open-url'), [url]], ['termux-open-url', [url]], ['xdg-open', [url]], ['wslview', [url]]];
  for (const [programa, args] of intentos) {
    if (programa.includes('/') ? !existsSync(programa) : !existe(programa)) continue;
    try {
      spawn(programa, args, { stdio: 'ignore', detached: true, windowsVerbatimArguments: WIN }).unref();
      return true;
    } catch { /* siguiente */ }
  }
  return false;
}

/** La terminal del sistema, para la opción «abrir una terminal aquí». */
export function shellDelSistema() {
  if (WIN) return process.env.ComSpec || 'cmd.exe';
  return process.env.SHELL || (existsSync('/bin/bash') ? '/bin/bash' : '/bin/sh');
}
