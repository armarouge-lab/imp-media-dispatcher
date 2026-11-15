#!/bin/sh
set -eu

MODE="user"
for arg in "$@"; do
    case "$arg" in
        --user) MODE="user" ;;
        --system) MODE="system" ;;
        -h|--help)
            cat <<'USAGE'
Usage: sh install.sh [--user|--system]

  --user     install into $HOME/.local (default)
  --system   install into /usr/local (requires root)
USAGE
            exit 0
            ;;
        *) echo "imp: unknown arg: $arg" >&2; exit 64 ;;
    esac
done

if [ "$MODE" = "system" ] && [ "$(id -u)" -ne 0 ]; then
    echo "imp: --system requires root" >&2
    exit 1
fi

if ! command -v java >/dev/null 2>&1 || ! command -v javac >/dev/null 2>&1; then
    echo "imp: Java 17+ (JDK, not just JRE) not found in PATH" >&2
    exit 1
fi

PKG_INSTALL=""
case "$(uname -s)" in
    Linux*)
        if [ -f /etc/debian_version ]; then
            . ./scripts/install-debian.sh
        else
            echo "imp: unsupported Linux distribution (only Debian-based)" >&2
            exit 1
        fi
        ;;
    FreeBSD) . ./scripts/install-freebsd.sh ;;
    OpenBSD) . ./scripts/install-openbsd.sh ;;
    *) echo "imp: unsupported OS: $(uname -s)" >&2; exit 1 ;;
esac

echo "Building imp.jar..."
./build.sh

mkdir -p "$PREFIX/lib/imp" "$PREFIX/bin" "$CONFDIR"
cp imp.jar "$PREFIX/lib/imp/imp.jar"

cat > "$PREFIX/bin/imp" <<LAUNCHER
#!/bin/sh
exec java -jar "$PREFIX/lib/imp/imp.jar" "\$@"
LAUNCHER
chmod +x "$PREFIX/bin/imp"

if [ ! -f "$CONFDIR/imp.conf" ]; then
    cp resources/imp.conf.default "$CONFDIR/imp.conf"
    echo "installed: $CONFDIR/imp.conf"
else
    echo "kept:      $CONFDIR/imp.conf"
fi
if [ ! -f "$CONFDIR/mime.types" ]; then
    cp resources/mime.types.default "$CONFDIR/mime.types"
    echo "installed: $CONFDIR/mime.types"
else
    echo "kept:      $CONFDIR/mime.types"
fi

if [ -f resources/imp.1 ]; then
    mkdir -p "$PREFIX/man/man1"
    cp resources/imp.1 "$PREFIX/man/man1/imp.1"
    echo "installed: $PREFIX/man/man1/imp.1"
fi

if ! command -v xdg-open >/dev/null 2>&1; then
    echo "" >&2
    echo "WARNING: xdg-open not found. The fallback will not work." >&2
    if [ -t 0 ]; then
        printf "Continue anyway? [y/N] "
        read answer
        case "$answer" in
            y|Y) ;;
            *) echo "aborted"; exit 1 ;;
        esac
    fi
fi

if [ -t 0 ] && [ -n "$PKG_INSTALL" ]; then
    echo ""
    echo "Optional handlers (recommended for common formats):"
    for pkg in mpv imv zathura; do
        if command -v "$pkg" >/dev/null 2>&1; then
            echo "  $pkg: already installed"
            continue
        fi
        printf "  install %s? [y/N] " "$pkg"
        read answer
        case "$answer" in
            y|Y)
                if [ "$MODE" = "system" ]; then
                    if ! $PKG_INSTALL "$pkg"; then
                        echo "imp: failed to install $pkg" >&2
                    fi
                else
                    if ! sudo $PKG_INSTALL "$pkg"; then
                        echo "imp: failed to install $pkg (need sudo?)" >&2
                    fi
                fi
                ;;
            *) echo "  skipped $pkg" ;;
        esac
    done

    if ! command -v libreoffice >/dev/null 2>&1; then
        echo ""
        printf "  install libreoffice? (big, ~300MB, for docx/xlsx/pptx) [y/N] "
        read answer
        case "$answer" in
            y|Y)
                if [ "$MODE" = "system" ]; then
                    if ! $PKG_INSTALL libreoffice; then
                        echo "imp: failed to install libreoffice" >&2
                    fi
                else
                    if ! sudo $PKG_INSTALL libreoffice; then
                        echo "imp: failed to install libreoffice (need sudo?)" >&2
                    fi
                fi
                ;;
            *) echo "  skipped libreoffice" ;;
        esac
    fi
fi

echo ""
echo "imp installed at $PREFIX/bin/imp"
echo "config at $CONFDIR/imp.conf"
