#!/bin/sh
# Sourced by install.sh. Do not execute directly.
if [ "$MODE" = "system" ]; then
    PREFIX=/usr/local
    CONFDIR=/etc/redhot/imp
    if command -v apt-get >/dev/null 2>&1; then
        apt-get install -y openjdk-17-jdk xdg-utils file || true
    fi
else
    PREFIX="$HOME/.local"
    CONFDIR="$HOME/.config/redhot/imp"
    echo "imp: user mode — ensure openjdk-17-jdk, xdg-utils, file are installed" >&2
fi
