#!/bin/sh
# Lanzador de bridge-ai para Linux, macOS y Termux (sigue enlaces simbólicos hasta la carpeta del repositorio).
f="$0"
while [ -L "$f" ]; do
  d=$(cd "$(dirname "$f")" && pwd)
  f=$(readlink "$f")
  case "$f" in /*) ;; *) f="$d/$f" ;; esac
done
exec node "$(cd "$(dirname "$f")" && pwd)/bin/bridge-ai.mjs" "$@"
