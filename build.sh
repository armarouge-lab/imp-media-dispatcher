#!/bin/sh
set -eu
if ! command -v javac >/dev/null 2>&1; then
    echo "imp: javac not found in PATH" >&2
    exit 1
fi
rm -rf .build
mkdir -p .build
javac -d .build $(find src -name '*.java')
cp resources/*.default .build/
jar cfe imp.jar br.redhot.imp.Main -C .build .
size=$(du -h imp.jar | cut -f1)
echo "Built imp.jar ($size)"
