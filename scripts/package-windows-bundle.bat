@echo off
setlocal EnableExtensions

set "ROOT_DIR=%~dp0.."
for %%I in ("%ROOT_DIR%") do set "ROOT_DIR=%%~fI"
set "CACHE_DIR=%ROOT_DIR%\target\jre-cache"
set "JRE_ZIP=%CACHE_DIR%\windows-jre.zip"
set "JRE_URL=https://api.adoptium.net/v3/binary/latest/17/ga/windows/x64/jre/hotspot/normal/eclipse?project=jdk"

if not exist "%JRE_ZIP%" (
  if not exist "%CACHE_DIR%" mkdir "%CACHE_DIR%"
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri '%JRE_URL%' -OutFile '%JRE_ZIP%'"
  if errorlevel 1 exit /b %errorlevel%
)

where py >nul 2>nul
if not errorlevel 1 (
  py -3 "%ROOT_DIR%\scripts\package-windows-bundle.py" %*
) else (
  python "%ROOT_DIR%\scripts\package-windows-bundle.py" %*
)
exit /b %errorlevel%
