#!/bin/sh
# Instala Guarura en Linux, macOS o Termux: comprueba Node.js y enlaza los comandos «guarura» e «ia» en el PATH.
set -e
AQUI=$(cd "$(dirname "$0")" && pwd)

if ! command -v node >/dev/null 2>&1; then
  echo "Falta Node.js (20.18 o más nuevo)."
  if [ -n "$TERMUX_VERSION" ]; then echo "  pkg install nodejs-lts git"
  elif command -v brew >/dev/null 2>&1; then echo "  brew install node git"
  elif command -v apt >/dev/null 2>&1; then echo "  sudo apt install nodejs npm git   (o https://nodejs.org para una versión reciente)"
  else echo "  https://nodejs.org"; fi
  exit 1
fi
node -e 'const [a,b]=process.versions.node.split(".").map(Number);process.exit(a>20||(a===20&&b>=18)?0:1)' || {
  echo "Tu Node.js es $(node -v): Guarura necesita 20.18 o más nuevo (https://nodejs.org)."; exit 1; }
command -v git >/dev/null 2>&1 || echo "Aviso: falta git (hace falta para instalar FreeLLMAPI desde Guarura)."

chmod +x "$AQUI/guarura" "$AQUI/ia" "$AQUI/bin/guarura.mjs"
if [ -n "$PREFIX" ] && [ -d "$PREFIX/bin" ] && [ -w "$PREFIX/bin" ]; then DESTINO="$PREFIX/bin"
elif [ -w /usr/local/bin ]; then DESTINO=/usr/local/bin
else
  DESTINO="$HOME/.local/bin"
  mkdir -p "$DESTINO"
  case ":$PATH:" in *":$DESTINO:"*) ;; *)
    for rc in "$HOME/.bashrc" "$HOME/.zshrc" "$HOME/.profile"; do
      [ -f "$rc" ] || [ "$rc" = "$HOME/.profile" ] || continue
      grep -q '.local/bin' "$rc" 2>/dev/null || printf '\nexport PATH="$HOME/.local/bin:$PATH"\n' >> "$rc"
    done
    echo "Añadido $DESTINO al PATH: abre una terminal nueva (o ejecuta: export PATH=\"\$HOME/.local/bin:\$PATH\")." ;;
  esac
fi
ln -sf "$AQUI/guarura" "$DESTINO/guarura"
ln -sf "$AQUI/guarura" "$DESTINO/ia"
echo "Listo: $DESTINO/guarura (y el atajo ia) → $AQUI/guarura"
echo "Escribe:  guarura"
