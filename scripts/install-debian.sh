#!/bin/sh
if [ "$MODE" = "system" ]; then
    PREFIX=/usr/local
    CONFDIR=/etc/redhot/imp
    PKG_INSTALL="apt-get install -y"
    if command -v apt-get >/dev/null 2>&1; then
        apt-get install -y openjdk-17-jdk xdg-utils file || true
    fi
else
    PREFIX="$HOME/.local"
    CONFDIR="$HOME/.config/redhot/imp"
    PKG_INSTALL="apt-get install -y"
    echo "imp: user mode — optional packages need sudo" >&2
fi
