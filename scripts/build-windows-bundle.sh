#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

APP_NAME="brasfoot-save-editor"
JRE_URL="${JRE_URL:-https://api.adoptium.net/v3/binary/latest/17/ga/windows/x64/jre/hotspot/normal/eclipse?project=jdk}"
TARGET_DIR="${PROJECT_DIR}/target"
DIST_ROOT="${TARGET_DIR}/dist"
BUNDLE_DIR="${DIST_ROOT}/${APP_NAME}-windows"
CACHE_DIR="${TARGET_DIR}/jre-cache"
JRE_ZIP="${CACHE_DIR}/windows-jre.zip"
JRE_UNPACK_DIR="${CACHE_DIR}/windows-jre-unpacked"
ZIP_PATH="${TARGET_DIR}/${APP_NAME}-windows.zip"

cd "${PROJECT_DIR}"

if command -v mvn >/dev/null 2>&1; then
  mvn package
elif command -v nix-shell >/dev/null 2>&1; then
  nix-shell --run "mvn package"
else
  printf 'mvn not found. Install Maven or run through nix-shell.\n' >&2
  exit 1
fi

mkdir -p "${CACHE_DIR}"
if [ ! -s "${JRE_ZIP}" ]; then
  curl -L --fail --output "${JRE_ZIP}" "${JRE_URL}"
fi

rm -rf "${BUNDLE_DIR}" "${JRE_UNPACK_DIR}" "${ZIP_PATH}"
mkdir -p "${BUNDLE_DIR}" "${JRE_UNPACK_DIR}"

unzip -q "${JRE_ZIP}" -d "${JRE_UNPACK_DIR}"
jre_roots=("${JRE_UNPACK_DIR}"/*)
if [ "${#jre_roots[@]}" -ne 1 ] || [ ! -d "${jre_roots[0]}" ]; then
  printf 'Unexpected JRE archive layout: %s\n' "${JRE_ZIP}" >&2
  exit 1
fi

cp "${TARGET_DIR}/${APP_NAME}.exe" "${BUNDLE_DIR}/"
mv "${jre_roots[0]}" "${BUNDLE_DIR}/runtime"

(
  cd "${DIST_ROOT}"
  zip -qr "${ZIP_PATH}" "${APP_NAME}-windows"
)

printf 'Windows bundle created: %s\n' "${ZIP_PATH}"
