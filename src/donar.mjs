// Donaciones y firma. El QR se dibuja en la terminal con medios bloques (dos filas por línea).
import { createRequire } from 'node:module';
import { negrita, tenue } from './ui.mjs';

const qrcode = createRequire(import.meta.url)('../lib/qrcode.cjs');

export const DONACIONES = [
  { id: 'btc', nombre: 'Bitcoin (BTC)', red: 'red Bitcoin', direccion: 'bc1qd9j45f4t0jwyhjhqh2kvz2cr7k8xye460rr2y7', uri: d => `bitcoin:${d}` },
  { id: 'xmr', nombre: 'Monero (XMR)', red: 'red Monero', direccion: '447gTj6Hg6gaAEAUmjqfhqDZr1PziUTvbT4LYLpmLVnTNVFK6cqeqPfh6P4neMKLWX5jDXAr94fWHacJwDvjmCzBBH8wPBt', uri: d => `monero:${d}` },
  { id: 'usdc', nombre: 'USDC', red: 'red Arbitrum One · mínimo 5 USDC', direccion: '0xAC43f9b0E7F0ae6386cfC8ef6BFf62CE9c3006A1', uri: d => d },
];
export const FIRMA = 'Hecho xhumanx & IA';
export const REPO = 'https://github.com/humanx123/bridge-ai';

function matriz(texto) {
  const q = qrcode(0, 'M');
  q.addData(texto);
  q.make();
  const n = q.getModuleCount();
  return { n, oscuro: (f, c) => f >= 0 && c >= 0 && f < n && c < n && q.isDark(f, c) };
}

/** QR para la terminal: blanco sobre negro invertido para que lo lean las cámaras en temas oscuros. */
export function qrTerminal(texto) {
  const { n, oscuro } = matriz(texto);
  const lineas = [];
  for (let f = -2; f < n + 2; f += 2) {
    let l = '';
    for (let c = -2; c < n + 2; c++) {
      const arriba = oscuro(f, c);
      const abajo = oscuro(f + 1, c);
      l += arriba && abajo ? ' ' : arriba ? '▄' : abajo ? '▀' : '█';
    }
    lineas.push(l);
  }
  return lineas.join('\n');
}

/** QR como SVG (para la página y el README). */
export function qrSvg(texto, modulo = 6) {
  const { n, oscuro } = matriz(texto);
  const margen = 4;
  const lado = (n + margen * 2) * modulo;
  let ruta = '';
  for (let f = 0; f < n; f++) for (let c = 0; c < n; c++) if (oscuro(f, c)) ruta += `M${(c + margen) * modulo},${(f + margen) * modulo}h${modulo}v${modulo}h-${modulo}z`;
  return `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 ${lado} ${lado}" width="${lado}" height="${lado}" shape-rendering="crispEdges"><rect width="100%" height="100%" fill="#fff"/><path d="${ruta}" fill="#000"/></svg>\n`;
}

export function mostrarDonaciones({ conQr = true } = {}) {
  console.log(negrita('Apoya el proyecto'));
  console.log(`Si bridge-ai te sirve, dale una ★ en GitHub: ${REPO}\n`);
  for (const d of DONACIONES) {
    console.log(negrita(d.nombre), tenue(`(${d.red})`));
    if (conQr) console.log(qrTerminal(d.uri(d.direccion)));
    console.log(d.direccion, '\n');
  }
  console.log('¡Gracias!');
  console.log(tenue(FIRMA));
}
