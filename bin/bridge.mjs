#!/usr/bin/env node
// bridge-ai — un menú para usar Claude Code y Codex con modelos gratis a través de FreeLLMAPI.
// Funciona igual en Windows (cmd o PowerShell), macOS, Linux y Termux. «ia» es un atajo del mismo comando.
//   bridge-ai            menú
//   bridge-ai claude     Claude Code con FreeLLMAPI          bridge-ai codex      Codex con FreeLLMAPI
//   bridge-ai instalar   instala FreeLLMAPI                  bridge-ai estado     estado y modelos
//   bridge-ai encender | apagar | proveedores | donar | ayuda | version
// Las credenciales solo se pasan al proceso de Claude/Codex: no se tocan ~/.claude ni ~/.codex.
import { existsSync, readFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';
import { cargar, guardar, POR_DEFECTO, rutaConfig } from '../src/config.mjs';
import * as gw from '../src/gateway.mjs';
import { enlaces, Panel, sinClave } from '../src/panel.mjs';
import { FIRMA, mostrarDonaciones, REPO } from '../src/donar.mjs';
import { abrirEnlace, ejecutar, existe, NOMBRE_SISTEMA, shellDelSistema } from '../src/sistema.mjs';
import { amarillo, cian, confirmar, elegir, enmascarar, limpiar, negrita, pausa, preguntar, rojo, tenue, verde } from '../src/ui.mjs';

const RAIZ = join(dirname(fileURLToPath(import.meta.url)), '..');
const VERSION = JSON.parse(readFileSync(join(RAIZ, 'package.json'), 'utf8')).version;
const [mayor, menor] = process.versions.node.split('.').map(Number);
if (mayor < 20 || (mayor === 20 && menor < 18)) {
  console.error(`bridge-ai necesita Node.js 20.18 o más nuevo (tienes ${process.versions.node}). Descárgalo en https://nodejs.org`);
  process.exit(1);
}

let cfg = cargar();
const panel = () => new Panel(cfg);

// ── Gateway ──────────────────────────────────────────────────────────────
/** Deja FreeLLMAPI listo: encendido y con una clave que acepta. Si falta algo, guía al usuario. */
async function asegurarGateway() {
  if (!(await gw.encendido(cfg))) {
    if (gw.instalado(cfg)) await gw.encender(cfg);
    else {
      console.log(amarillo(`FreeLLMAPI no responde en ${cfg.url} y no está instalado en este equipo.`));
      if (await confirmar('¿Instalarlo ahora? (descarga y compila, unos minutos)')) {
        if (await gw.instalar(cfg)) await gw.encender(cfg);
      } else console.log(`Si ya lo tienes en otro sitio (p. ej. la app de escritorio en :3001), cambia la URL en Configuración.`);
    }
  }
  if (!(await gw.encendido(cfg))) return false;
  if (await gw.claveValida(cfg)) return true;
  console.log(amarillo('FreeLLMAPI está encendido pero falta la clave unificada (o ya no vale). La busco en el panel…'));
  if (!(await asegurarSesion())) return false;
  try {
    cfg.apiKey = await panel().claveUnificada();
    guardar(cfg);
  } catch (e) { console.log(rojo(`No se pudo leer la clave: ${e.message}`)); }
  if (await gw.claveValida(cfg)) { console.log(verde('Clave unificada guardada.')); return true; }
  console.log(rojo('La clave sigue sin funcionar. Cópiala del panel (página Keys) en Configuración → API key.'));
  return false;
}

/** Inicia sesión en el panel de FreeLLMAPI (o crea la cuenta la primera vez) y guarda el token. */
async function asegurarSesion() {
  let estado;
  try { estado = await panel().estado(); } catch (e) { console.log(rojo(`El panel no responde: ${e.message}`)); return false; }
  if (estado === 'dentro') return true;
  const nueva = estado === 'necesitaCuenta';
  console.log(negrita(nueva ? 'Crea la cuenta del panel de FreeLLMAPI (solo esta vez)' : 'Inicia sesión en el panel de FreeLLMAPI'));
  const email = await preguntar('Email: ');
  if (!email) return false;
  const pass = await preguntar(nueva ? 'Contraseña (mín. 8): ' : 'Contraseña: ', { oculto: true });
  if (nueva && pass !== await preguntar('Repite la contraseña: ', { oculto: true })) { console.log(rojo('No coinciden.')); return false; }
  try {
    cfg.tokenPanel = nueva ? await panel().crearCuenta(email, pass) : await panel().entrar(email, pass);
    guardar(cfg);
  } catch (e) { console.log(rojo(e.message)); return false; }
  console.log(verde('Sesión iniciada.'));
  if (nueva) {
    console.log('Activando los proveedores gratis que no piden clave…');
    (await panel().activarGratis().catch(e => [rojo(e.message)])).forEach(l => console.log(l));
  }
  return true;
}

/** Ejecuta una operación del panel; si la sesión caducó, pide entrar otra vez y la repite. */
async function conSesion(fn) {
  if (!(await asegurarSesion())) return null;
  try { return await fn(panel()); } catch (e) {
    if (!e.sesion) throw e;
    cfg.tokenPanel = '';
    guardar(cfg);
    if (!(await asegurarSesion())) return null;
    return fn(panel());
  }
}

// ── Claude Code y Codex ──────────────────────────────────────────────────
const PAQUETES = { claude: '@anthropic-ai/claude-code', codex: '@openai/codex' };

async function asegurarPrograma(programa) {
  if (existe(programa)) return true;
  console.log(amarillo(`${programa} no está instalado.`));
  if (!(await confirmar(`¿Instalarlo ahora con npm (npm install -g ${PAQUETES[programa]})?`))) return false;
  await ejecutar('npm', ['install', '-g', PAQUETES[programa]]);
  return existe(programa);
}

const carpetaTrabajo = () => (existsSync(cfg.carpeta) ? cfg.carpeta : process.cwd());

async function lanzar(programa) {
  if (!(await asegurarGateway())) return false;
  if (!(await asegurarPrograma(programa))) return false;
  const modelo = programa === 'claude' ? cfg.modeloClaude : cfg.modeloCodex;
  console.log(cian(`${programa === 'claude' ? 'Claude Code' : 'Codex'} → FreeLLMAPI${modelo ? ` (${modelo})` : ''} en ${carpetaTrabajo()}`));
  await ejecutar('npx', ['-y', gw.CLI_FREELLMAPI, programa === 'claude' ? 'launch' : 'launch-codex', '--url', cfg.url, '--api-key', cfg.apiKey, ...(modelo ? ['--model', modelo] : [])], { cwd: carpetaTrabajo() });
  return true;
}

async function lanzarNormal(programa) {
  if (!(await asegurarPrograma(programa))) return;
  await ejecutar(programa, [], { cwd: carpetaTrabajo() });
}

async function elegirModelo(programa) {
  if (!(await asegurarGateway())) return pausa();
  const ids = await gw.modelos(cfg);
  if (!ids.length) { console.log(rojo('FreeLLMAPI no devolvió modelos. Añade proveedores primero.')); return pausa(); }
  const i = await elegir(`Modelos para ${programa === 'claude' ? 'Claude Code' : 'Codex'}:`, ids, { cero: 'Automático (FreeLLMAPI elige y cambia si uno falla)' });
  if (i === -1) { console.log('Sin cambios.'); return pausa(); }
  const sel = i === -2 ? '' : ids[i];
  if (programa === 'claude') cfg.modeloClaude = sel; else cfg.modeloCodex = sel;
  guardar(cfg);
  console.log(verde(`Guardado: ${sel || 'automático'}`));
  await pausa();
}

// ── Proveedores ──────────────────────────────────────────────────────────
async function agregarClave() {
  const datos = await conSesion(p => p.proveedores());
  if (!datos) return;
  const lista = datos.providers;
  const links = enlaces(cfg.freellmapi);
  const i = await elegir('Proveedores  (✔ configurado · ○ funciona sin clave)', lista.map(p => `${p.configured ? verde('✔') : sinClave(p) ? '○' : ' '} ${p.name}${p.kind === 'search' ? tenue(' [búsqueda]') : ''}`));
  if (i < 0) return;
  const p = lista[i];
  console.log(`\n${negrita(p.name)} (${p.platform})`);
  const url = links[p.platform];
  if (url) {
    console.log(`Consigue la clave aquí: ${cian(url)}`);
    if (await confirmar('¿Abrir el enlace en el navegador?')) console.log(abrirEnlace(url) ? verde('Abierto.') : `Ábrelo a mano: ${url}`);
  }
  const clave = await preguntar('Pega la API key (vacío para cancelar): ', { oculto: true });
  if (!clave) return;
  try {
    const r = await conSesion(pn => pn.agregar(p.platform, clave));
    if (r) console.log(verde(`Guardada (${r.maskedKey || 'ok'}). Modelos disponibles: ${r.modelsAvailable ?? '?'}${r.notice ? ` — ${r.notice}` : ''}`));
  } catch (e) { console.log(rojo(e.message)); }
}

async function verClaves({ probar = false, quitar = false } = {}) {
  const claves = await conSesion(p => p.claves());
  if (!claves) return;
  if (!claves.length) { console.log('No hay claves guardadas.'); return; }
  const fila = k => `${k.platform.padEnd(14)} ${String(k.maskedKey || (k.keyless ? 'sin clave' : '')).padEnd(16)} ${k.enabled ? (k.status === 'healthy' ? verde(k.status) : amarillo(k.status || '?')) : tenue('desactivada')}${k.label ? tenue(` · ${k.label}`) : ''}`;
  if (!probar && !quitar) { claves.forEach(k => console.log(fila(k))); return; }
  const i = await elegir(probar ? '¿Qué clave pruebo?' : '¿Qué clave quito?', claves.map(fila));
  if (i < 0) return;
  const k = claves[i];
  try {
    if (probar) console.log(`${k.platform}: ${await conSesion(p => p.probar(k.id))}`);
    else if (await confirmar(`¿Quitar la clave de ${k.platform}?`, false)) { await conSesion(p => p.quitar(k.id)); console.log(verde('Quitada.')); }
  } catch (e) { console.log(rojo(e.message)); }
}

async function menuProveedores() {
  if (!(await gw.encendido(cfg)) && !(await asegurarGateway())) return pausa();
  if (!(await asegurarSesion())) return pausa();
  for (;;) {
    limpiar();
    console.log(negrita('Proveedores y claves API (FreeLLMAPI)'));
    console.log('  1) Activar los gratis sin clave (Kilo, OVH, LLM7, AI Horde…)');
    console.log('  2) Añadir la clave de un proveedor (con el enlace para conseguirla)');
    console.log('  3) Ver claves guardadas');
    console.log('  4) Probar una clave');
    console.log('  5) Quitar una clave');
    console.log('  6) Abrir el panel web');
    console.log('  7) Cerrar sesión del panel');
    console.log('  0) Volver');
    const o = await preguntar('> ');
    try {
      if (o === '1') (await conSesion(p => p.activarGratis()))?.forEach(l => console.log(l));
      else if (o === '2') await agregarClave();
      else if (o === '3') await verClaves();
      else if (o === '4') await verClaves({ probar: true });
      else if (o === '5') await verClaves({ quitar: true });
      else if (o === '6') console.log(abrirEnlace(cfg.url) ? verde('Abierto.') : `Ábrelo a mano: ${cfg.url}`);
      else if (o === '7') { cfg.tokenPanel = ''; guardar(cfg); console.log('Sesión cerrada.'); await pausa(); return; }
      else return;
    } catch (e) { console.log(rojo(e.message)); }
    await pausa();
  }
}

// ── Estado y configuración ───────────────────────────────────────────────
async function estado() {
  const on = await gw.encendido(cfg);
  const ok = on && await gw.claveValida(cfg);
  console.log(negrita('Estado'));
  console.log(`  Sistema:        ${NOMBRE_SISTEMA} · Node ${process.versions.node} · bridge-ai ${VERSION}`);
  console.log(`  FreeLLMAPI:     ${cfg.url}  ${ok ? verde('● en línea') : on ? amarillo('● encendido, clave no válida') : rojo('● apagado')}`);
  console.log(`  Claude usa:     ${cfg.url}   Codex usa: ${cfg.url}/v1`);
  console.log(`  API key:        ${enmascarar(cfg.apiKey)}`);
  console.log(`  Modelo Claude:  ${cfg.modeloClaude || 'automático'}`);
  console.log(`  Modelo Codex:   ${cfg.modeloCodex || 'automático'}`);
  console.log(`  Carpeta:        ${cfg.carpeta}`);
  console.log(`  Instalación:    ${gw.instalado(cfg) ? cfg.freellmapi : tenue('no instalado por bridge-ai')}`);
  console.log(`  Configuración:  ${rutaConfig()}`);
  console.log(`  Claude Code: ${existe('claude') ? verde('instalado') : tenue('no instalado')} · Codex: ${existe('codex') ? verde('instalado') : tenue('no instalado')}`);
  if (ok) console.log(`  Modelos:        ${(await gw.modelos(cfg)).length}`);
}

async function menuConfiguracion() {
  for (;;) {
    limpiar();
    console.log(negrita('Configuración'));
    console.log(`  1) URL de FreeLLMAPI     (${cfg.url})`);
    console.log(`  2) API key unificada     (${enmascarar(cfg.apiKey)})`);
    console.log(`  3) Modelo para Claude    (${cfg.modeloClaude || 'automático'})`);
    console.log(`  4) Modelo para Codex     (${cfg.modeloCodex || 'automático'})`);
    console.log(`  5) Carpeta de trabajo    (${cfg.carpeta})`);
    console.log(`  6) Proveedores y claves API`);
    console.log(`  7) FreeLLMAPI: instalar o actualizar`);
    console.log(`  8) FreeLLMAPI: reiniciar    9) FreeLLMAPI: ver registro`);
    console.log(`  E) Encender FreeLLMAPI al abrir bridge-ai: ${cfg.encenderSolo ? verde('sí') : 'no'}`);
    console.log(`  R) Restablecer valores por defecto`);
    console.log('  0) Volver');
    const o = (await preguntar('> '))?.toLowerCase();
    if (o === '1') {
      const v = await preguntar('Nueva URL (sin /v1; la app de escritorio usa http://127.0.0.1:3001): ');
      if (v) { cfg.url = v.replace(/\/+$/, '').replace(/\/v1$/, ''); guardar(cfg); }
    } else if (o === '2') {
      const v = await preguntar('Nueva API key (vacío = leerla del panel): ', { oculto: true });
      if (v) { cfg.apiKey = v; guardar(cfg); } else if (await asegurarSesion()) {
        try { cfg.apiKey = await panel().claveUnificada(); guardar(cfg); console.log(verde('Leída del panel.')); } catch (e) { console.log(rojo(e.message)); }
        await pausa();
      }
    } else if (o === '3') await elegirModelo('claude');
    else if (o === '4') await elegirModelo('codex');
    else if (o === '5') {
      const v = await preguntar('Carpeta: ');
      if (v && existsSync(v)) { cfg.carpeta = v; guardar(cfg); } else if (v) { console.log(rojo('No existe.')); await pausa(); }
    } else if (o === '6') await menuProveedores();
    else if (o === '7') { await (gw.instalado(cfg) ? gw.actualizar(cfg) : gw.instalar(cfg)); await gw.encender(cfg); await pausa(); }
    else if (o === '8') { await gw.apagar(cfg); console.log((await gw.encender(cfg)) ? verde('Encendido.') : rojo('No arrancó.')); await pausa(); }
    else if (o === '9') { console.log(gw.ultimasLineas(cfg.freellmapi)); await pausa(); }
    else if (o === 'e') { cfg.encenderSolo = !cfg.encenderSolo; guardar(cfg); }
    else if (o === 'r') { if (await confirmar('¿Restablecer todo? (se conservan la clave y la sesión)', false)) { cfg = { ...POR_DEFECTO, apiKey: cfg.apiKey, tokenPanel: cfg.tokenPanel }; guardar(cfg); } }
    else return;
  }
}

async function laboratorio() {
  console.log(negrita('laboratorio.ia'), '— chat, investigación científica verificada e imágenes con IA');
  console.log(`  Pruébalo sin registrarte:  ${cian('https://lab-ia-demo.reserchlab.workers.dev')}`);
  console.log(`  Tu propio laboratorio:     ${cian('https://labia-ciencia.pages.dev')}`);
  try {
    const e = await (await fetch('http://127.0.0.1:8790/api/estado', { signal: AbortSignal.timeout(2000) })).json();
    console.log(verde(`  Encendido en este equipo: http://127.0.0.1:8790 (versión ${e.version})`));
  } catch { /* no corre aquí */ }
  if (await confirmar('¿Abrir la demo en el navegador?', false)) abrirEnlace('https://lab-ia-demo.reserchlab.workers.dev');
}

// ── Menú principal ───────────────────────────────────────────────────────
async function menu() {
  for (;;) {
    limpiar();
    const on = await gw.encendido(cfg);
    const st = on ? verde('● en línea') : rojo('● apagado');
    console.log(negrita(`╭─ bridge-ai ─ FreeLLMAPI ${st}${negrita(` ─ ${NOMBRE_SISTEMA} ─╮`)}`));
    console.log(`  1) Claude Code  ${amarillo('(FreeLLMAPI, modelos gratis)')}`);
    console.log(`  2) Codex        ${amarillo('(FreeLLMAPI, modelos gratis)')}`);
    console.log('  3) Claude Code  (tu cuenta de Anthropic)');
    console.log('  4) Codex        (tu cuenta de OpenAI)');
    console.log('  5) Configuración (URL, clave, modelos, carpeta, instalar)');
    console.log('  6) Estado');
    console.log('  7) Abrir una terminal aquí');
    console.log('  8) Proveedores y claves API (gratis, añadir claves)');
    console.log('  9) laboratorio.ia (chat e investigación científica)');
    console.log('  D) Donar · acerca de');
    console.log('  0) Salir');
    console.log(tenue(`  ${FIRMA}`));
    const o = (await preguntar('> '))?.toLowerCase();
    if (o == null || o === '0' || o === 'q') process.exit(0);
    if (o === '1') { if (!(await lanzar('claude'))) await pausa(); }
    else if (o === '2') { if (!(await lanzar('codex'))) await pausa(); }
    else if (o === '3') await lanzarNormal('claude');
    else if (o === '4') await lanzarNormal('codex');
    else if (o === '5') await menuConfiguracion();
    else if (o === '6') { await estado(); await pausa(); }
    else if (o === '7') { console.log(tenue('Escribe «exit» para volver al menú.')); await ejecutar(shellDelSistema(), [], { cwd: carpetaTrabajo() }); }
    else if (o === '8') await menuProveedores();
    else if (o === '9') { await laboratorio(); await pausa(); }
    else if (o === 'd') { limpiar(); mostrarDonaciones(); await pausa(); }
  }
}

const AYUDA = `${negrita(`bridge-ai ${VERSION}`)} — Claude Code y Codex con modelos gratis (FreeLLMAPI)
${tenue('La bridge-ai es la caracola que se sopla para llamar a la gente: esta llama a tus agentes.')}

  bridge-ai              menú interactivo  (atajo: ia)
  bridge-ai claude       Claude Code con FreeLLMAPI
  bridge-ai codex        Codex con FreeLLMAPI
  bridge-ai estado       estado, clave y modelos
  bridge-ai instalar     instala (o actualiza) FreeLLMAPI en este equipo
  bridge-ai encender     enciende FreeLLMAPI en segundo plano
  bridge-ai apagar       lo apaga
  bridge-ai proveedores  activa proveedores gratis y añade claves
  bridge-ai donar        BTC, Monero o USDC
  bridge-ai version

Configuración: ${rutaConfig()}
${REPO}
${tenue(FIRMA)}`;

const orden = (process.argv[2] || '').toLowerCase();
if (['ayuda', 'help', '-h', '--help'].includes(orden)) console.log(AYUDA);
else if (['version', '-v', '--version'].includes(orden)) console.log(VERSION);
else if (orden === 'donar') mostrarDonaciones();
else if (orden === 'estado') await estado();
else if (orden === 'instalar') { if (await (gw.instalado(cfg) ? gw.actualizar(cfg) : gw.instalar(cfg))) await asegurarGateway(); }
else if (orden === 'encender') console.log((await gw.encender(cfg)) ? verde(`FreeLLMAPI en línea: ${cfg.url}`) : rojo('No se pudo encender (¿instalado? prueba: bridge-ai instalar)'));
else if (orden === 'apagar') console.log((await gw.apagar(cfg)) ? 'FreeLLMAPI apagado.' : 'No estaba encendido por bridge-ai.');
else if (orden === 'proveedores') await menuProveedores();
else if (orden === 'claude' || orden === 'codex') process.exitCode = (await lanzar(orden)) ? 0 : 1;
else if (orden) { console.log(AYUDA); process.exitCode = 1; }
else {
  if (cfg.encenderSolo && gw.instalado(cfg)) await gw.encender(cfg, { silencioso: false });
  await menu();
}
