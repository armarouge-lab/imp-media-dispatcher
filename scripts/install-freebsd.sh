#!/bin/sh
# Sourced by install.sh. Do not execute directly.
if [ "$MODE" = "system" ]; then
    PREFIX=/usr/local
    CONFDIR=/usr/local/etc/redhot/imp
    if command -v pkg >/dev/null 2>&1; then
        pkg install -y openjdk17 xdg-utils || true
    fi
else
    PREFIX="$HOME/.local"
    CONFDIR="$HOME/.config/redhot/imp"
    echo "imp: user mode — ensure openjdk17 and xdg-utils are installed" >&2
fi
