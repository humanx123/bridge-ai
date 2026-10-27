// Configuración en un JSON solo legible por el usuario. Importa la del antiguo `ia` de Bash si existe.
import { chmodSync, existsSync, mkdirSync, readFileSync, writeFileSync } from 'node:fs';
import { homedir } from 'node:os';
import { join } from 'node:path';
import { carpetaConfig, carpetaDatos, WIN } from './sistema.mjs';

const ARCHIVO = join(carpetaConfig(), 'config.json');

export const POR_DEFECTO = {
  url: 'http://127.0.0.1:31415', // FreeLLMAPI (la app de escritorio usa :3001)
  apiKey: '', // clave unificada freellmapi-… (se obtiene sola al iniciar sesión)
  tokenPanel: '', // sesión del panel de FreeLLMAPI para gestionar proveedores
  modeloClaude: '',
  modeloCodex: '',
  carpeta: homedir(), // donde se abren Claude Code y Codex
  freellmapi: join(carpetaDatos(), 'freellmapi'), // instalación local que enciende ia
  encenderSolo: true,
};

/** El `ia` de Bash guardaba VARIABLE="valor" en ~/.config/ia/config: se aprovecha en la primera vez. */
function importarAntigua() {
  const vieja = join(carpetaConfig(), 'config');
  if (WIN || !existsSync(vieja)) return {};
  const v = Object.fromEntries(readFileSync(vieja, 'utf8').split('\n').map(l => l.match(/^(\w+)="?(.*?)"?$/)).filter(Boolean).map(m => [m[1], m[2]]));
  return {
    ...(v.FREELLMAPI_URL ? { url: v.FREELLMAPI_URL } : {}),
    ...(v.FREELLMAPI_API_KEY ? { apiKey: v.FREELLMAPI_API_KEY } : {}),
    ...(v.FREELLMAPI_DASHBOARD_TOKEN ? { tokenPanel: v.FREELLMAPI_DASHBOARD_TOKEN } : {}),
    ...(v.IA_MODEL_CLAUDE ? { modeloClaude: v.IA_MODEL_CLAUDE } : {}),
    ...(v.IA_MODEL_CODEX ? { modeloCodex: v.IA_MODEL_CODEX } : {}),
    ...(v.IA_DIR ? { carpeta: v.IA_DIR } : {}),
    ...(existsSync('/root/freellmapi/server') ? { freellmapi: '/root/freellmapi' } : {}),
  };
}

export function cargar() {
  if (!existsSync(ARCHIVO)) {
    const cfg = { ...POR_DEFECTO, ...importarAntigua() };
    guardar(cfg);
    return cfg;
  }
  try { return { ...POR_DEFECTO, ...JSON.parse(readFileSync(ARCHIVO, 'utf8')) }; } catch { return { ...POR_DEFECTO }; }
}

export function guardar(cfg) {
  mkdirSync(carpetaConfig(), { recursive: true });
  writeFileSync(ARCHIVO, JSON.stringify(cfg, null, 2) + '\n', { mode: 0o600 });
  try { chmodSync(ARCHIVO, 0o600); } catch { /* Windows: permisos del perfil del usuario */ }
}

export const rutaConfig = () => ARCHIVO;
