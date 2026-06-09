#!/usr/bin/env bash
# Compila y ejecuta la aplicacion (Linux / macOS / Git Bash)
# Uso: bash run.sh [sqlite|memoria]   (por defecto: sqlite)
set -e
# En Windows (Git Bash) pone la consola en UTF-8 para ver bien las tildes.
# En Linux/macOS este comando no existe y simplemente se ignora.
chcp.com 65001 >/dev/null 2>&1 || true

# Separador de classpath: ';' en Windows, ':' en Linux/macOS.
SEP=":"
case "$(uname -s)" in
    MINGW*|MSYS*|CYGWIN*) SEP=";" ;;
esac

mkdir -p out
javac -encoding UTF-8 -d out $(find src -name "*.java")
java --enable-native-access=ALL-UNNAMED -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -cp "out${SEP}lib/*" com.banco.externa.ui.Main "$@"
