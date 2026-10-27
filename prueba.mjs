// Pruebas rápidas sin red: arranca la CLI en una carpeta de configuración temporal.
import { spawnSync } from 'node:child_process';
import { mkdtempSync, readFileSync, rmSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import assert from 'node:assert/strict';

const tmp = mkdtempSync(join(tmpdir(), 'bridge-ai-prueba-'));
const env = { ...process.env, HOME: tmp, USERPROFILE: tmp, APPDATA: tmp, LOCALAPPDATA: tmp, XDG_CONFIG_HOME: join(tmp, 'cfg'), NO_COLOR: '1' };
const ia = (...a) => spawnSync(process.execPath, ['bin/bridge-ai.mjs', ...a], { env, encoding: 'utf8', input: '' });

try {
  const v = ia('version');
  assert.equal(v.status, 0);
  assert.equal(v.stdout.trim(), JSON.parse(readFileSync('package.json', 'utf8')).version);

  const ayuda = ia('ayuda');
  assert.match(ayuda.stdout, /bridge-ai claude/);
  assert.match(ayuda.stdout, /Hecho xhumanx & IA/);

  const donar = ia('donar');
  for (const d of ['bc1qd9j45f4t0jwyhjhqh2kvz2cr7k8xye460rr2y7', '0xAC43f9b0E7F0ae6386cfC8ef6BFf62CE9c3006A1', '447gTj6Hg6gaAEAUmjqfhqDZr1PziUTvbT4LYLpmLVnTNVFK6cqeqPfh6P4neMKLWX5jDXAr94fWHacJwDvjmCzBBH8wPBt']) assert.ok(donar.stdout.includes(d), d);
  assert.match(donar.stdout, /█/);

  // El menú termina limpio cuando se cierra la entrada.
  const menu = spawnSync(process.execPath, ['bin/bridge-ai.mjs'], { env: { ...env }, encoding: 'utf8', input: '0\n', timeout: 20_000 });
  assert.equal(menu.status, 0, menu.stderr);
  assert.match(menu.stdout, /Claude Code/);

  const desconocida = ia('xyz');
  assert.equal(desconocida.status, 1);
  console.log('ok');
} finally {
  rmSync(tmp, { recursive: true, force: true });
}
