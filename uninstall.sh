#!/bin/sh
set -eu

MODE="user"
PURGE=0
for arg in "$@"; do
    case "$arg" in
        --user) MODE="user" ;;
        --system) MODE="system" ;;
        --keep) PURGE=0 ;;
        --purge) PURGE=1 ;;
        -h|--help)
            cat <<'USAGE'
Usage: sh uninstall.sh [--user|--system] [--keep|--purge]

  --user     remove from $HOME/.local (default)
  --system   remove from /usr/local
  --keep     preserve config (default)
  --purge    remove config too
USAGE
            exit 0
            ;;
        *) echo "imp: unknown arg: $arg" >&2; exit 64 ;;
    esac
done

case "$(uname -s)" in
    Linux*) CONFDIR_SYS=/etc/redhot/imp ;;
    FreeBSD) CONFDIR_SYS=/usr/local/etc/redhot/imp ;;
    OpenBSD) CONFDIR_SYS=/etc/redhot/imp ;;
    *) echo "imp: unsupported OS"; exit 1 ;;
esac

if [ "$MODE" = "system" ]; then
    PREFIX=/usr/local
    CONFDIR="$CONFDIR_SYS"
    if [ "$(id -u)" -ne 0 ]; then
        echo "imp: --system requires root" >&2
        exit 1
    fi
else
    PREFIX="$HOME/.local"
    CONFDIR="$HOME/.config/redhot/imp"
fi

rm -f "$PREFIX/bin/imp"
rm -f "$PREFIX/lib/imp/imp.jar"
rmdir "$PREFIX/lib/imp" 2>/dev/null || true
rm -f "$PREFIX/man/man1/imp.1"

if [ "$PURGE" = "1" ]; then
    if [ -t 0 ]; then
        printf "remove %s? [y/N] " "$CONFDIR"
        read answer
        case "$answer" in
            y|Y) rm -rf "$CONFDIR"; echo "removed: $CONFDIR" ;;
            *) echo "kept: $CONFDIR" ;;
        esac
    else
        echo "imp: non-interactive, keeping $CONFDIR" >&2
    fi
fi

echo "imp removed from $PREFIX"
