#!/bin/sh
set -eu
mkdir -p .build
javac -d .build $(find src -name '*.java')
jar cfe imp.jar br.redhot.imp.Main -C .build .
echo "Built imp.jar"