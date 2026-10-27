// Colores, preguntas y menús en la terminal (sin dependencias).
import { createInterface } from 'node:readline';

const color = !process.env.NO_COLOR && process.stdout.isTTY;
const c = codigo => (texto => (color ? `\x1b[${codigo}m${texto}\x1b[0m` : String(texto)));
export const negrita = c('1');
export const verde = c('32');
export const rojo = c('31');
export const amarillo = c('33');
export const cian = c('36');
export const tenue = c('2');

export function limpiar() {
  if (process.stdout.isTTY) process.stdout.write('\x1b[2J\x1b[3J\x1b[H');
}

// Una sola lectura de la entrada para toda la sesión, con cola: si se pegan varias líneas
// (o la entrada viene de un archivo) ninguna se pierde entre pregunta y pregunta.
let rl = null;
let soltando = false;
let cerrada = false;
let silencio = false;
const cola = [];
const esperando = [];

function entrada() {
  if (rl) return rl;
  rl = createInterface({ input: process.stdin, output: process.stdout, terminal: Boolean(process.stdin.isTTY) });
  rl._writeToOutput = s => { if (!silencio) rl.output.write(s); };
  rl.on('line', l => { const r = esperando.shift(); if (r) r(l); else cola.push(l); });
  rl.on('close', () => {
    rl = null;
    if (soltando) { soltando = false; return; }
    cerrada = true;
    while (esperando.length) esperando.shift()(null);
  });
  return rl;
}

/** Suelta la terminal para un programa hijo (Claude Code, Codex, npm…); la siguiente pregunta la retoma. */
export function soltarEntrada() {
  if (!rl) return;
  soltando = true;
  rl.close();
}

/** Pregunta y devuelve el texto escrito; null si se cerró la entrada (Ctrl+D / fin de archivo). */
export function preguntar(pregunta, { oculto = false } = {}) {
  if (cerrada) {
    // Entrada cerrada: primero las líneas que ya llegaron, luego null.
    if (cola.length) { process.stdout.write(`${pregunta}${oculto ? '' : cola[0]}\n`); return Promise.resolve(cola.shift().trim()); }
    return Promise.resolve(null);
  }
  const r = entrada();
  r.setPrompt(pregunta);
  r.prompt();
  // Oculto: no se muestra lo que se escribe (contraseñas y claves).
  if (oculto && process.stdin.isTTY) silencio = true;
  return new Promise(resolve => {
    const fin = l => {
      if (silencio) { silencio = false; process.stdout.write('\n'); }
      resolve(l === null ? null : l.trim());
    };
    if (cola.length) fin(cola.shift()); else esperando.push(fin);
  });
}

export async function pausa() {
  await preguntar(tenue('Enter para volver al menú…'));
}

export async function confirmar(pregunta, porDefecto = true) {
  const r = await preguntar(`${pregunta} ${porDefecto ? '[S/n]' : '[s/N]'} `);
  if (r === null || r === '') return porDefecto;
  return /^[sy]/i.test(r);
}

/** Elige de una lista numerada. Devuelve el índice o -1. */
export async function elegir(titulo, opciones, { cero } = {}) {
  console.log(negrita(titulo));
  if (cero) console.log(`  0) ${cero}`);
  opciones.forEach((o, i) => console.log(`${String(i + 1).padStart(3)}) ${o}`));
  const r = await preguntar('Número: ');
  const n = Number(r);
  if (cero && r === '0') return -2;
  return Number.isInteger(n) && n >= 1 && n <= opciones.length ? n - 1 : -1;
}

export const enmascarar = clave => (clave ? `${clave.slice(0, 14)}…` : tenue('(sin clave)'));
