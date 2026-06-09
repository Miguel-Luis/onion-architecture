@echo off
REM Compila y ejecuta la aplicacion (Windows)
REM Uso: run.bat [sqlite|memoria]   (por defecto: sqlite)
chcp 65001 >nul
setlocal enabledelayedexpansion
if not exist out mkdir out

set SOURCES=
for /r src %%f in (*.java) do set SOURCES=!SOURCES! "%%f"

javac -encoding UTF-8 -d out !SOURCES!
if errorlevel 1 (
    echo Error de compilacion
    exit /b 1
)
java --enable-native-access=ALL-UNNAMED -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -cp "out;lib/*" com.banco.externa.ui.Main %*
