// Panel de FreeLLMAPI: cuenta, clave unificada y proveedores (las mismas rutas que usa su web).
import { readFileSync } from 'node:fs';
import { join } from 'node:path';

// Enlaces para conseguir la clave de cada proveedor (copia de los del panel de FreeLLMAPI 0.8;
// si FreeLLMAPI está instalado localmente se leen de su código, que puede tener más).
export const ENLACES = {
  google: 'https://aistudio.google.com/apikey', groq: 'https://console.groq.com/keys', cerebras: 'https://cloud.cerebras.ai',
  nvidia: 'https://build.nvidia.com/settings/api-keys', mistral: 'https://console.mistral.ai/api-keys/', openrouter: 'https://openrouter.ai/keys',
  cohere: 'https://dashboard.cohere.com/api-keys', cloudflare: 'https://dash.cloudflare.com', zhipu: 'https://z.ai/manage-apikey/apikey-list',
  ollama: 'https://ollama.com/settings/keys', kilo: 'https://app.kilo.ai', pollinations: 'https://enter.pollinations.ai',
  ovh: 'https://endpoints.ai.cloud.ovh.net', llm7: 'https://llm7.io', huggingface: 'https://huggingface.co/settings/tokens',
  opencode: 'https://opencode.ai/auth', agnes: 'https://platform.agnes-ai.com', reka: 'https://platform.reka.ai',
  siliconflow: 'https://siliconflow.com', routeway: 'https://routeway.ai', requesty: 'https://www.requesty.ai',
  modelscope: 'https://modelscope.cn/my/myaccesstoken', aihorde: 'https://aihorde.net/register', moondream: 'https://moondream.ai/c/cloud/api-keys',
  electronhub: 'https://app.electronhub.ai', typhoon: 'https://playground.opentyphoon.ai', sealion: 'https://sea-lion.ai',
  longcat: 'https://longcat.chat/platform', volcengine: 'https://console.volcengine.com/ark', qianfan: 'https://console.bce.baidu.com/qianfan/overview',
  tavily: 'https://app.tavily.com/home', exa: 'https://dashboard.exa.ai/api-keys', firecrawl: 'https://www.firecrawl.dev/app/api-keys',
  linkup: 'https://app.linkup.so/api-keys', parallel: 'https://platform.parallel.ai', brave: 'https://api-dashboard.search.brave.com/app/keys',
  keenable: 'https://keenable.ai', serpsearch: 'https://serpsearch.com/dashboard/api-keys',
};

// Proveedores que aceptan uso anónimo aunque el servidor no los marque como «sin clave».
const ANONIMOS = { llm7: 'unused' };

export function enlaces(dirFreellmapi) {
  const todos = { ...ENLACES };
  const re = /value:\s*'([\w-]+)',\s*label:\s*'(?:[^'\\]|\\.)*',\s*url:\s*'([^']+)'/g;
  for (const f of ['client/src/components/keys/shared.tsx', 'client/src/lib/search.ts']) {
    try { for (const m of readFileSync(join(dirFreellmapi, f), 'utf8').matchAll(re)) todos[m[1]] = m[2]; } catch { /* sin código local */ }
  }
  return todos;
}

export class Panel {
  constructor(cfg) { this.cfg = cfg; }

  async api(metodo, ruta, cuerpo) {
    const r = await fetch(this.cfg.url + ruta, {
      method: metodo,
      headers: { 'Content-Type': 'application/json', ...(this.cfg.tokenPanel ? { Authorization: `Bearer ${this.cfg.tokenPanel}` } : {}) },
      body: cuerpo ? JSON.stringify(cuerpo) : undefined,
      signal: AbortSignal.timeout(60_000),
    });
    const d = await r.json().catch(() => ({}));
    if (r.status === 401) throw Object.assign(new Error('La sesión del panel caducó o la contraseña no es correcta.'), { sesion: true });
    if (!r.ok) throw new Error(d?.error?.message || d?.error || `HTTP ${r.status}`);
    return d;
  }

  /** necesitaCuenta | dentro | entrar */
  async estado() {
    const s = await this.api('GET', '/api/auth/status');
    return s.needsSetup ? 'necesitaCuenta' : s.authenticated || s.desktop ? 'dentro' : 'entrar';
  }
  async crearCuenta(email, password) { return (await this.api('POST', '/api/auth/setup', { email, password })).token; }
  async entrar(email, password) { return (await this.api('POST', '/api/auth/login', { email, password })).token; }
  async claveUnificada() { return (await this.api('GET', '/api/settings/api-key')).apiKey; }

  async proveedores() { return this.api('GET', '/api/keys/providers'); }
  async claves() { return this.api('GET', '/api/keys'); }
  async agregar(platform, key) { return this.api('POST', '/api/keys', { platform, key }); }
  async quitar(id) { return this.api('DELETE', `/api/keys/${id}`); }
  async probar(id) { return (await this.api('POST', `/api/health/check/${id}`)).status; }

  /** Activa los proveedores gratis que no piden clave. Devuelve líneas para mostrar. */
  async activarGratis() {
    const { providers } = await this.proveedores();
    const lineas = [];
    for (const p of providers.filter(p => p.keyless || p.platform in ANONIMOS)) {
      if (p.configured) { lineas.push(`  = ${p.name}: ya estaba`); continue; }
      const key = ANONIMOS[p.platform];
      try {
        await this.api('POST', '/api/keys', { platform: p.platform, label: 'anónimo', ...(key ? { key } : {}) });
        lineas.push(`  + ${p.name}: activado sin clave`);
      } catch (e) { lineas.push(`  ! ${p.name}: ${e.message}`); }
    }
    return lineas;
  }
}

export const sinClave = p => p.keyless || p.platform in ANONIMOS;
