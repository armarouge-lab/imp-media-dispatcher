#!/bin/sh
if [ "$MODE" = "system" ]; then
    PREFIX=/usr/local
    CONFDIR=/etc/redhot/imp
    PKG_INSTALL="pkg_add"
    if command -v pkg_add >/dev/null 2>&1; then
        pkg_add openjdk17 xdg-utils || true
    fi
else
    PREFIX="$HOME/.local"
    CONFDIR="$HOME/.config/redhot/imp"
    PKG_INSTALL="pkg_add"
    echo "imp: user mode — optional packages need root" >&2
fi
