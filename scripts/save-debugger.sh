#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SAVE_PATH="${1:-$HOME/Documentos/Brasfoot22-23/sav/not.s22}"
BRASFOOT_FILES="${2:-$ROOT_DIR/brasfoot_files}"

run_inner() {
  cd "$ROOT_DIR"
  if [[ ! -f target/classes/br/com/saveeditor/brasfoot/debug/SaveDebugger.class ]]; then
    mvn -q -DskipTests compile
  fi
  if [[ ! -f target/classpath.txt ]]; then
    mvn -q dependency:build-classpath -Dmdep.outputFile=target/classpath.txt
  fi
  CP="target/classes:$(cat target/classpath.txt)"
  exec java -cp "$CP" br.com.saveeditor.brasfoot.debug.SaveDebugger "$SAVE_PATH" "$BRASFOOT_FILES"
}

if command -v mvn >/dev/null 2>&1; then
  run_inner
elif command -v nix-shell >/dev/null 2>&1; then
  export ROOT_DIR SAVE_PATH BRASFOOT_FILES
  exec nix-shell "$ROOT_DIR/shell.nix" --run 'bash "$ROOT_DIR/scripts/save-debugger.sh" "$SAVE_PATH" "$BRASFOOT_FILES"'
else
  echo "Precisa de Maven no PATH ou nix-shell disponível." >&2
  exit 1
fi
