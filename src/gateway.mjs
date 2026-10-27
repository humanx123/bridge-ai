// FreeLLMAPI: instalarlo, encenderlo en segundo plano, apagarlo y comprobarlo.
import { spawn, spawnSync } from 'node:child_process';
import { randomBytes } from 'node:crypto';
import { closeSync, existsSync, mkdirSync, openSync, readFileSync, rmSync, writeFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { ejecutar, existe, WIN } from './sistema.mjs';
import { amarillo, cian, rojo, verde } from './ui.mjs';

export const REPO_FREELLMAPI = 'https://github.com/tashfeenahmed/freellmapi';
export const CLI_FREELLMAPI = 'freellmapi@0.8.2';

const espera = ms => new Promise(r => setTimeout(r, ms));
const entrada = dir => join(dir, 'server', 'dist', 'index.js');
export const archivoLog = dir => join(dir, 'logs', 'server.log');
const archivoPid = dir => join(dir, 'logs', 'server.pid');

async function pedir(url, opciones = {}, ms = 5000) {
  try { return await fetch(url, { ...opciones, signal: AbortSignal.timeout(ms) }); } catch { return null; }
}

/** ¿Responde el servidor? (no comprueba la clave) */
export async function encendido(cfg) {
  return Boolean((await pedir(`${cfg.url}/api/ping`, {}, 3000))?.ok);
}

/** ¿Acepta la clave unificada? */
export async function claveValida(cfg) {
  if (!cfg.apiKey) return false;
  const r = await pedir(`${cfg.url}/v1/models`, { headers: { Authorization: `Bearer ${cfg.apiKey}` } }, 8000);
  return r?.status === 200;
}

export async function modelos(cfg) {
  const r = await pedir(`${cfg.url}/v1/models`, { headers: { Authorization: `Bearer ${cfg.apiKey}` } }, 10_000);
  if (!r?.ok) return [];
  const d = await r.json().catch(() => ({}));
  return (d.data || []).map(m => m.id).filter(Boolean);
}

export const instalado = cfg => existsSync(entrada(cfg.freellmapi));

function leerEnv(dir) {
  try {
    return Object.fromEntries(readFileSync(join(dir, '.env'), 'utf8').split(/\r?\n/)
      .map(l => l.match(/^\s*([A-Z0-9_]+)\s*=\s*"?(.*?)"?\s*$/)).filter(Boolean).map(m => [m[1], m[2]]));
  } catch { return {}; }
}

const puertoDe = url => { try { return new URL(url).port || '80'; } catch { return '31415'; } };

/** Clona y compila FreeLLMAPI en la carpeta de datos (unos minutos la primera vez). */
export async function instalar(cfg) {
  for (const p of ['git', 'npm']) {
    if (!existe(p)) {
      console.log(rojo(`Falta ${p}.`), WIN ? 'Instálalo con: winget install Git.Git  (y Node.js: winget install OpenJS.NodeJS.LTS)' : 'Instálalo con tu gestor de paquetes (apt, brew, pkg…).');
      return false;
    }
  }
  const dir = cfg.freellmapi;
  if (!existsSync(join(dir, 'package.json'))) {
    mkdirSync(dirname(dir), { recursive: true });
    console.log(cian(`Descargando FreeLLMAPI en ${dir}…`));
    if (await ejecutar('git', ['clone', '--depth', '1', REPO_FREELLMAPI, dir])) return false;
  }
  console.log(cian('Instalando dependencias (puede tardar unos minutos)…'));
  if (await ejecutar('npm', ['install', '--no-audit', '--no-fund'], { cwd: dir })) return false;
  console.log(cian('Compilando el servidor y el panel…'));
  if (await ejecutar('npm', ['run', 'build', '-w', 'server'], { cwd: dir })) return false;
  if (await ejecutar('npm', ['run', 'build', '-w', 'client'], { cwd: dir })) console.log(amarillo('El panel web no se compiló; el servidor funciona igual.'));
  if (!existsSync(join(dir, '.env'))) {
    // ipv4first: muchas redes (sobre todo móviles) no enrutan IPv6 y cada conexión perdería segundos.
    writeFileSync(join(dir, '.env'), [
      `ENCRYPTION_KEY=${randomBytes(32).toString('hex')}`, `PORT=${puertoDe(cfg.url)}`, 'HOST=127.0.0.1',
      'NODE_ENV=production', 'NODE_OPTIONS=--dns-result-order=ipv4first', '',
    ].join('\n'), { mode: 0o600 });
  }
  console.log(verde('FreeLLMAPI instalado.'));
  return true;
}

export async function actualizar(cfg) {
  const dir = cfg.freellmapi;
  if (!existsSync(join(dir, '.git'))) { console.log(rojo('No hay una instalación de FreeLLMAPI hecha por bridge-ai.')); return false; }
  await apagar(cfg);
  if (await ejecutar('git', ['pull', '--ff-only'], { cwd: dir })) return false;
  return instalar(cfg);
}

/** Enciende el servidor en segundo plano (sigue vivo al cerrar ia). */
export async function encender(cfg, { silencioso = false } = {}) {
  if (await encendido(cfg)) return true;
  const dir = cfg.freellmapi;
  if (!existsSync(entrada(dir))) return false;
  if (!silencioso) console.log(cian('Encendiendo FreeLLMAPI…'));
  mkdirSync(join(dir, 'logs'), { recursive: true });
  const log = openSync(archivoLog(dir), 'a');
  const env = { ...process.env, ...leerEnv(dir) };
  const hijo = spawn(process.execPath, [entrada(dir)], { cwd: dir, env, detached: true, stdio: ['ignore', log, log], windowsHide: true });
  hijo.unref();
  closeSync(log);
  writeFileSync(archivoPid(dir), String(hijo.pid));
  for (let i = 0; i < 45; i++) {
    if (await encendido(cfg)) return true;
    await espera(1000);
  }
  console.log(rojo('No arrancó. Últimas líneas del registro:'));
  console.log(ultimasLineas(dir, 15));
  return false;
}

export async function apagar(cfg) {
  const pid = Number(existsSync(archivoPid(cfg.freellmapi)) ? readFileSync(archivoPid(cfg.freellmapi), 'utf8') : 0);
  if (!pid) return false;
  try {
    if (WIN) spawnSync('taskkill', ['/PID', String(pid), '/T', '/F'], { stdio: 'ignore' });
    else process.kill(pid);
  } catch { /* ya estaba apagado */ }
  rmSync(archivoPid(cfg.freellmapi), { force: true });
  for (let i = 0; i < 10 && await encendido(cfg); i++) await espera(500);
  return true;
}

export function ultimasLineas(dir, n = 40) {
  try { return readFileSync(archivoLog(dir), 'utf8').trimEnd().split('\n').slice(-n).join('\n'); } catch { return '(sin registro)'; }
}
