// FreeLLMAPI: instalarlo, encenderlo en segundo plano, apagarlo y comprobarlo.
import { spawn, spawnSync } from 'node:child_process';
import { randomBytes } from 'node:crypto';
import { closeSync, existsSync, mkdirSync, openSync, readdirSync, readFileSync, rmSync, writeFileSync } from 'node:fs';
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

// El archivo de PID puede estar desfasado (lo escribe también el `ia` de Termux, que guarda el PID de un
// envoltorio y no el del servidor). Por eso el servidor real se busca por el puerto que escucha.

/** PIDs de los servidores de FreeLLMAPI que atienden en el puerto de la configuración. */
function pidsDeFreellmapi(cfg) {
  const puerto = Number(puertoDe(cfg.url));
  if (WIN) {
    const r = spawnSync('netstat', ['-ano', '-p', 'TCP'], { encoding: 'utf8' });
    const linea = (r.stdout || '').split(/\r?\n/).find(l => l.includes(`:${puerto} `) && /LISTENING|ESCUCHANDO/i.test(l));
    return linea ? [Number(linea.trim().split(/\s+/).pop())] : [];
  }
  if (process.platform === 'linux') {
    // Termux y Linux: no hace falta mirar el socket (puede estar vedado). Cada servidor de FreeLLMAPI
    // lleva en su entorno PORT=<puerto> desde el .env, así que se identifica por eso.
    const pids = [];
    for (const pid of readdirSync('/proc').filter(p => /^\d+$/.test(p))) {
      try {
        if (!/server\/dist\/index\.js/.test(readFileSync(`/proc/${pid}/cmdline`, 'utf8'))) continue;
        if (readFileSync(`/proc/${pid}/environ`, 'utf8').split('\0').includes(`PORT=${puerto}`)) pids.push(Number(pid));
      } catch { /* el proceso ya terminó o no es legible */ }
    }
    return pids;
  }
  const r = spawnSync('lsof', ['-nP', `-iTCP:${puerto}`, '-sTCP:LISTEN', '-t'], { encoding: 'utf8' });
  return (r.stdout || '').split('\n').filter(Boolean).map(Number);
}

/** Línea de comandos de un proceso. */
function comandoDe(pid) {
  if (WIN) return spawnSync('powershell', ['-NoProfile', '-Command', `(Get-CimInstance Win32_Process -Filter "ProcessId=${pid}").CommandLine`], { encoding: 'utf8' }).stdout || '';
  if (existsSync(`/proc/${pid}/cmdline`)) return readFileSync(`/proc/${pid}/cmdline`, 'utf8').replace(/\0/g, ' ');
  return spawnSync('ps', ['-o', 'args=', '-p', String(pid)], { encoding: 'utf8' }).stdout || '';
}

/** ¿Es un servidor de FreeLLMAPI? Así no se mata nada ajeno que use el mismo puerto. */
const esFreellmapi = pid => /server[\\/]dist[\\/]index\.js/.test(comandoDe(pid));

/** Enciende el servidor en segundo plano (sigue vivo al cerrar ia). */
export async function encender(cfg, { silencioso = false } = {}) {
  if (await encendido(cfg)) {
    // Ya está en marcha (quizá lo arrancó otro lanzador): se anota su PID real para poder apagarlo luego.
    const [vivo] = pidsDeFreellmapi(cfg);
    if (vivo && existsSync(cfg.freellmapi)) writeFileSync(archivoPid(cfg.freellmapi), String(vivo));
    return true;
  }
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

/** Apaga FreeLLMAPI. Devuelve true solo si ya no responde en su puerto. */
export async function apagar(cfg) {
  const dir = cfg.freellmapi;
  const candidatos = new Set([Number(existsSync(archivoPid(dir)) ? readFileSync(archivoPid(dir), 'utf8') : 0), ...pidsDeFreellmapi(cfg)]);
  for (const pid of candidatos) {
    if (!pid || !esFreellmapi(pid)) continue;
    try {
      if (WIN) spawnSync('taskkill', ['/PID', String(pid), '/T', '/F'], { stdio: 'ignore' });
      else process.kill(pid, 'SIGTERM');
    } catch { /* ya se había ido */ }
  }
  rmSync(archivoPid(dir), { force: true });
  // El servidor termina las peticiones en curso antes de cerrar: se espera hasta 20 s.
  for (let i = 0; i < 40 && await encendido(cfg); i++) await espera(500);
  return !(await encendido(cfg));
}

export function ultimasLineas(dir, n = 40) {
  try { return readFileSync(archivoLog(dir), 'utf8').trimEnd().split('\n').slice(-n).join('\n'); } catch { return '(sin registro)'; }
}
