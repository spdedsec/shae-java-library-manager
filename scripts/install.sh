#!/usr/bin/env bash
#
# scripts/install.sh — shae-java-library-manager
#
# Installs and runs the Java library manager.
#
# SHAE design system note: the tool prints to the console using the paper /
# ink / oxide palette (#F2EEE5 / #191712 / #B3341B) via ANSI escapes.
#
# Requires: javac + java (JDK). Not available locally; install a JDK first.
#
# Usage:
#   ./scripts/install.sh           # compile
#   ./scripts/install.sh run       # compile + run help
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SRC="$REPO_ROOT/src/LibraryManager.java"
CLASS_DIR="$REPO_ROOT/build/classes"

require_java() {
    if ! command -v javac >/dev/null 2>&1 || ! command -v java >/dev/null 2>&1; then
        echo "Error: javac/java (JDK) are required but not installed." >&2
        echo "Install a JDK (e.g. sudo apt install default-jdk) and re-run." >&2
        exit 1
    fi
}

compile() {
    require_java
    mkdir -p "$CLASS_DIR"
    javac -d "$CLASS_DIR" "$SRC"
    echo "Compiled: $SRC -> $CLASS_DIR"
}

run() {
    compile
    java -cp "$CLASS_DIR" LibraryManager "${@:2}"
}

case "${1:-}" in
    run)
        shift
        run "$@"
        ;;
    *)
        compile
        echo "Done. Run with: java -cp build/classes LibraryManager <command> [args]"
        echo "Example: java -cp build/classes LibraryManager add \"Title\" \"Author\" \"ISBN-123\""
        ;;
esac
