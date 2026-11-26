#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
CACHE_DIR="$ROOT_DIR/target/jre-cache"
JRE_ZIP="$CACHE_DIR/windows-jre.zip"
JRE_URL="${JRE_URL:-https://api.adoptium.net/v3/binary/latest/17/ga/windows/x64/jre/hotspot/normal/eclipse?project=jdk}"

mkdir -p "$CACHE_DIR"
if [[ ! -s "$JRE_ZIP" ]]; then
  command -v curl >/dev/null 2>&1 || { echo "curl is required to download the Java 17 runtime." >&2; exit 1; }
  curl -L --fail --output "$JRE_ZIP" "$JRE_URL"
fi

python3 "$ROOT_DIR/scripts/package-windows-bundle.py" "$@"
