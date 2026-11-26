@echo off
setlocal EnableExtensions

set "ROOT_DIR=%~dp0.."
for %%I in ("%ROOT_DIR%") do set "ROOT_DIR=%%~fI"
set "APP_NAME=brasfoot-save-editor"
set "CHOICE=%~1"

if /I "%CHOICE%"=="-h" goto usage
if /I "%CHOICE%"=="--help" goto usage

if "%CHOICE%"=="" (
  echo Formato de compilacao:
  echo   1. JAR multiplataforma
  echo   2. Executavel Windows ^(.exe^)
  echo   3. Ambos
  echo   4. Executavel Windows com pacote debug ^(JAR e BAT^)
  set /p "MENU_CHOICE=Escolha 1, 2, 3 ou 4: "
  call set "CHOICE=%%MENU_CHOICE%%"
)

if "%CHOICE%"=="1" set "CHOICE=jar"
if "%CHOICE%"=="2" set "CHOICE=exe"
if "%CHOICE%"=="3" set "CHOICE=both"
if "%CHOICE%"=="4" set "CHOICE=exe-debug"

set "LAUNCH4J_SKIP=false"
if /I "%CHOICE%"=="jar" set "LAUNCH4J_SKIP=true"
if /I not "%CHOICE%"=="jar" if /I not "%CHOICE%"=="exe" if /I not "%CHOICE%"=="both" if /I not "%CHOICE%"=="exe-debug" goto usage_error

cd /d "%ROOT_DIR%"
where mvn >nul 2>nul
if errorlevel 1 (
  echo Maven nao encontrado no PATH. Instale Maven e Java 17.
  exit /b 1
)

call mvn -DskipTests -Dlaunch4j.skip=%LAUNCH4J_SKIP% package
if errorlevel 1 exit /b %errorlevel%

if /I "%CHOICE%"=="exe" call "%ROOT_DIR%\scripts\package-windows-bundle.bat"
if errorlevel 1 exit /b %errorlevel%
if /I "%CHOICE%"=="both" call "%ROOT_DIR%\scripts\package-windows-bundle.bat"
if errorlevel 1 exit /b %errorlevel%
if /I "%CHOICE%"=="exe-debug" call "%ROOT_DIR%\scripts\package-windows-bundle.bat" --debug
if errorlevel 1 exit /b %errorlevel%

echo Build concluido:
if /I "%CHOICE%"=="jar" echo   %ROOT_DIR%\target\%APP_NAME%.jar
if /I "%CHOICE%"=="exe" echo   %ROOT_DIR%\target\%APP_NAME%.exe ^(Windows^)
if /I "%CHOICE%"=="exe" echo   %ROOT_DIR%\target\%APP_NAME%-windows.zip ^(EXE + Java 17; sem JAR/BAT^)
if /I "%CHOICE%"=="exe-debug" echo   %ROOT_DIR%\target\%APP_NAME%-windows-debug.zip ^(EXE + Java 17 + JAR + BAT^)
if /I "%CHOICE%"=="both" echo   %ROOT_DIR%\target\%APP_NAME%.jar
if /I "%CHOICE%"=="both" echo   %ROOT_DIR%\target\%APP_NAME%.exe ^(Windows^)
if /I "%CHOICE%"=="both" echo   %ROOT_DIR%\target\%APP_NAME%-windows.zip ^(EXE + Java 17; sem JAR/BAT^)
echo O ZIP inclui Java 17 para executar sem instalacao separada.
exit /b 0

:usage
echo Uso: scripts\build.bat [jar^|exe^|exe-debug^|both]
echo Sem argumento, abre um menu de selecao.
echo   jar   compila o JAR multiplataforma
echo   exe   compila o executavel Windows
echo   exe-debug compila o executavel e inclui JAR e BAT de debug no ZIP
echo   both  compila os dois formatos
exit /b 0

:usage_error
echo Opcao invalida. Use jar, exe, both, 1, 2 ou 3.
exit /b 2
