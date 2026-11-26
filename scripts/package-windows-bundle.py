from pathlib import Path
from shutil import copy2, move, rmtree
from zipfile import ZIP_DEFLATED, ZipFile
import argparse


parser = argparse.ArgumentParser()
parser.add_argument("--debug", action="store_true", help="include the application JAR and debug launcher")
args = parser.parse_args()


ROOT = Path(__file__).resolve().parents[1]
TARGET = ROOT / "target"
CACHE = TARGET / "jre-cache"
UNPACKED = CACHE / "windows-jre-unpacked"
DIST = TARGET / "dist"
BUNDLE_NAME = "brasfoot-save-editor-windows-debug" if args.debug else "brasfoot-save-editor-windows"
BUNDLE = DIST / BUNDLE_NAME
ARCHIVE_NAME = "brasfoot-save-editor-windows-debug.zip" if args.debug else "brasfoot-save-editor-windows.zip"
ARCHIVE = TARGET / ARCHIVE_NAME
RUNTIME_ARCHIVE = CACHE / "windows-jre.zip"
EXECUTABLE = TARGET / "brasfoot-save-editor.exe"
APPLICATION_JAR = TARGET / "brasfoot-save-editor.jar"
DEBUG_LAUNCHER = ROOT / "scripts" / "run-windows-debug.bat"

if not EXECUTABLE.is_file():
    raise SystemExit(f"Windows executable not found: {EXECUTABLE}")
if args.debug and not APPLICATION_JAR.is_file():
    raise SystemExit(f"Application JAR not found: {APPLICATION_JAR}")
if not RUNTIME_ARCHIVE.is_file():
    raise SystemExit(f"Java 17 runtime archive not found: {RUNTIME_ARCHIVE}")

rmtree(UNPACKED, ignore_errors=True)
rmtree(BUNDLE, ignore_errors=True)
ARCHIVE.unlink(missing_ok=True)
UNPACKED.mkdir(parents=True)

with ZipFile(RUNTIME_ARCHIVE) as runtime_zip:
    runtime_zip.extractall(UNPACKED)

runtime_roots = list(UNPACKED.iterdir())
if len(runtime_roots) != 1 or not runtime_roots[0].is_dir():
    raise SystemExit(f"Unexpected Java runtime archive layout: {runtime_roots}")

BUNDLE.mkdir(parents=True)
copy2(EXECUTABLE, BUNDLE / EXECUTABLE.name)
if args.debug:
    copy2(APPLICATION_JAR, BUNDLE / APPLICATION_JAR.name)
    copy2(DEBUG_LAUNCHER, BUNDLE / "run-debug.bat")
move(str(runtime_roots[0]), str(BUNDLE / "runtime"))

with ZipFile(ARCHIVE, "w", ZIP_DEFLATED, compresslevel=6) as bundle_zip:
    for path in BUNDLE.rglob("*"):
        if path.is_file():
            bundle_zip.write(path, path.relative_to(DIST))

print(f"Windows executable: {BUNDLE / EXECUTABLE.name}")
contents = "EXE + Java 17 + JAR + debug BAT" if args.debug else "EXE + Java 17"
print(f"Windows package ({contents}): {ARCHIVE} ({ARCHIVE.stat().st_size:,} bytes)")
