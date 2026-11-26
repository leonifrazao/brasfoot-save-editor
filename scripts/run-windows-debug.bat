@echo off
setlocal EnableExtensions

pushd "%~dp0"
set "JAVA=%~dp0runtime\bin\java.exe"
set "JAR=%~dp0brasfoot-save-editor.jar"
set "LOG_DIR=%~dp0logs"
set "EXIT_CODE=1"

if not exist "%JAVA%" (
  echo Java 17 runtime not found: "%JAVA%"
  goto finish
)
if not exist "%JAR%" (
  echo Application JAR not found: "%JAR%"
  goto finish
)
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"

echo Starting Brasfoot Save Editor with Java 17...
echo Console output and errors will appear below.
echo.
"%JAVA%" -Djava.awt.headless=false "-DLOG_DIR=%LOG_DIR%" -jar "%JAR%"
set "EXIT_CODE=%ERRORLEVEL%"
echo.
echo Application exited with code %EXIT_CODE%.
echo If created, logs are in "%LOG_DIR%\brasfoot-save-editor.jsonl".

:finish
echo.
pause
popd
exit /b %EXIT_CODE%
