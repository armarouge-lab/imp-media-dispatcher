#!/bin/sh
set -eu
mkdir -p .build
javac -d .build $(find src -name '*.java')
cp resources/*.default .build/ 2>/dev/null || true
jar cfe imp.jar br.redhot.imp.Main -C .build .
echo "Built imp.jar"
