# Imp

Media dispatcher for the Redhot suite.

Receives a file path, decides which application should open it, and delegates.

## Status

`v0.9.0` — functional and installable on Debian. V1.0.0 tag is pending validation
on FreeBSD (per spec §11: "Validação em Debian e FreeBSD, obrigatório para release").

The `.jar` is identical across all supported OSes (portability is in the Java code,
not in the install scripts).

## Requirements

- Java 17 LTS or later (JDK, not just JRE — needed for build)
- `xdg-open` (from `xdg-utils`) — universal fallback
- `file` (optional, via `libmagic`) — more accurate MIME detection
- Recommended handlers: `mpv`, `imv`, `zathura` (asked during install)

## Build

    ./build.sh

Produces `imp.jar` (no external dependencies).

## Install

    sh install.sh --user        # $HOME/.local (default, recommended on ephemeral systems)
    sudo sh install.sh --system # /usr/local  (production)

The installer detects the OS, asks about optional handlers (`mpv`, `imv`, `zathura`),
and places config in the OS-appropriate location.

## Uninstall

    sh uninstall.sh --keep      # remove binary, keep config
    sh uninstall.sh --purge     # remove everything (with confirmation)

## Usage

    imp <file>                  # dispatch the file
    imp --dry-run <file>        # show plan without executing
    imp --list-mappings         # show every loaded rule
    imp --list-handlers         # show handlers + availability
    imp --selftest              # report on environment
    imp --version
    imp --help

### Modifiers

    --config <path>             # alternate imp.conf
    --debug                     # verbose logging to stderr
    --json                      # JSON output (--list-*/--selftest)

## Configuration

- User config: `~/.config/redhot/imp/imp.conf`
- System config: `/etc/redhot/imp/imp.conf` (Debian/OpenBSD)
- System config: `/usr/local/etc/redhot/imp/imp.conf` (FreeBSD)

Format: `chave=valor`, one per line, `#` for comments, UTF-8.
Rules: `ext.<suffix>=<command>` or `mime.<type/subtype>=<command>`.
`mime.*` wins over `ext.*` when both match.

Auxiliary `mime.types` (Apache format) is consulted only when `file` is unavailable
or when `detect_mime=false`.

See `resources/imp.conf.default` for the full reference.

## Exit codes

| Code | Name | Meaning |
|------|------|---------|
| 0 | EX_OK | Handler executed successfully, or dry-run completed |
| 1 | EX_NO_HANDLER | No rule matched and no fallback configured |
| 2 | EX_EXEC_FAILED | All handlers in the chain failed |
| 3 | EX_PRECOND | File missing, unreadable, or is a directory |
| 4 | EX_CONFIG_ERROR | Configuration invalid |
| 5 | EX_CANCELED | Interrupted (SIGINT) |
| 64 | EX_USAGE | Usage error |

## Test

    ./build.sh
    sh tests/run.sh                  # unit tests
    sh tests/integration/run.sh      # end-to-end

The test suite has zero dependencies (no JUnit). Each test class has a static
`run(TestRunner)` method invoked from `AllTests`.

## MiniOS / ephemeral systems

On ephemeral systems (e.g. MiniOS), run `install.sh --user` at the start of each
session:

    sh install.sh --user

This reinstalls `~/.local/bin/imp` and preserves any existing
`~/.config/redhot/imp/imp.conf`.

## Limitations (V1.0)

- No `open` without argument (no file picker). That's Dragonet's job.
- No configurable fallback chain — one command only.
- No MIME detection by content when `file` is absent (falls back to extension).
- No timeout on handler (Imp waits for the handler to finish).
- No URL support. That's Nether's job.
- No GUI. It's CLI by design.
- No sandbox (documented in spec §9.5).

## License

BSD 2-Clause. See `LICENSE`.
