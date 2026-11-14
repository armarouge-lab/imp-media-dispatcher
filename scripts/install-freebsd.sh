#!/bin/sh
if [ "$MODE" = "system" ]; then
    PREFIX=/usr/local
    CONFDIR=/usr/local/etc/redhot/imp
    PKG_INSTALL="pkg install -y"
    if command -v pkg >/dev/null 2>&1; then
        pkg install -y openjdk17 xdg-utils || true
    fi
else
    PREFIX="$HOME/.local"
    CONFDIR="$HOME/.config/redhot/imp"
    PKG_INSTALL="pkg install -y"
    echo "imp: user mode — optional packages need root" >&2
fi
