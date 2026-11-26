#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
APP_NAME="brasfoot-save-editor"

usage() {
  cat <<'EOF'
Uso: scripts/build.sh [jar|exe|exe-debug|both]

Sem argumento, abre um menu de selecao no terminal.
  jar   compila o JAR multiplataforma
  exe   compila o executavel Windows
  exe-debug compila o executavel e gera pacote debug com JAR e BAT
  both  compila os dois formatos
EOF
}

choice="${1:-}"
if [[ "$choice" == "-h" || "$choice" == "--help" ]]; then
  usage
  exit 0
fi

if [[ -z "$choice" ]]; then
  echo "Formato de compilacao:"
  select option in "JAR multiplataforma" "Executavel Windows (.exe)" "Ambos"; do
    case "$REPLY" in
      1) choice="jar"; break ;;
      2) choice="exe"; break ;;
      3) choice="both"; break ;;
      4) choice="exe-debug"; break ;;
      *) echo "Escolha 1, 2, 3 ou 4." ;;
    esac
  done
fi

choice="${choice,,}"
case "$choice" in
  1) choice="jar" ;;
  2) choice="exe" ;;
  3) choice="both" ;;
  4) choice="exe-debug" ;;
esac

case "$choice" in
  jar) launch4j_skip=true ;;
  exe|exe-debug|both) launch4j_skip=false ;;
  *) usage >&2; exit 2 ;;
esac

cd "$ROOT_DIR"
maven_args=(-DskipTests "-Dlaunch4j.skip=${launch4j_skip}" package)
if command -v mvn >/dev/null 2>&1; then
  mvn "${maven_args[@]}"
elif command -v nix-shell >/dev/null 2>&1; then
  printf -v maven_command '%q ' mvn "${maven_args[@]}"
  nix-shell "$ROOT_DIR/shell.nix" --run "$maven_command"
else
  echo "Maven is required. Install Maven or use Nix with nix-shell." >&2
  exit 1
fi

if [[ "$choice" == "exe" || "$choice" == "both" ]]; then
  "$ROOT_DIR/scripts/package-windows-bundle.sh"
elif [[ "$choice" == "exe-debug" ]]; then
  "$ROOT_DIR/scripts/package-windows-bundle.sh" --debug
fi

echo "Build completed:"
case "$choice" in
  jar) echo "  $ROOT_DIR/target/${APP_NAME}.jar" ;;
  exe)
    echo "  $ROOT_DIR/target/${APP_NAME}.exe (Windows)"
    echo "  $ROOT_DIR/target/${APP_NAME}-windows.zip (EXE + Java 17; sem JAR/BAT)"
    ;;
  exe-debug)
    echo "  $ROOT_DIR/target/${APP_NAME}.exe (Windows)"
    echo "  $ROOT_DIR/target/${APP_NAME}-windows-debug.zip (EXE + Java 17 + JAR + BAT)"
    ;;
  both)
    echo "  $ROOT_DIR/target/${APP_NAME}.jar"
    echo "  $ROOT_DIR/target/${APP_NAME}.exe (Windows)"
    echo "  $ROOT_DIR/target/${APP_NAME}-windows.zip (EXE + Java 17; sem JAR/BAT)"
    ;;
esac
echo "The executable needs Java 17, unless distributed with the runtime bundle."
