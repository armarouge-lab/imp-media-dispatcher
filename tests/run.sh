#!/bin/sh
set -eu

# Compila classes de main (já feito pelo build.sh) + classes de teste
mkdir -p .build-tests
javac -cp .build -d .build-tests $(find tests -name '*.java')

# Roda
java -cp .build:.build-tests br.redhot.imp.AllTests
