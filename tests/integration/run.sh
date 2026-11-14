#!/bin/sh
set -eu
cd "$(dirname "$0")/../.."
./build.sh > /dev/null

PASS=0
FAIL=0

check() {
    name="$1"; expected="$2"; actual="$3"
    if [ "$expected" = "$actual" ]; then
        PASS=$((PASS + 1))
    else
        FAIL=$((FAIL + 1))
        printf 'FAIL [%s]: expected=%s actual=%s\n' "$name" "$expected" "$actual" >&2
    fi
}

TESTDIR=$(mktemp -d)
trap 'rm -rf "$TESTDIR"' EXIT
echo "hello" > "$TESTDIR/test.txt"
mkdir -p "$TESTDIR/subdir"

cat > "$TESTDIR/imp.conf" <<CEOF
version=1
fallback=cat
ext.txt=cat
CEOF

out=$(java -jar imp.jar --version 2>/dev/null)
check "version" "imp 0.9.0" "$out"

rc=0
java -jar imp.jar --selftest > /dev/null 2>&1 || rc=$?
check "selftest" "0" "$rc"

rc=0
java -jar imp.jar "$TESTDIR/subdir" > /dev/null 2>&1 || rc=$?
check "directory" "3" "$rc"

rc=0
java -jar imp.jar "$TESTDIR/missing.txt" > /dev/null 2>&1 || rc=$?
check "missing" "3" "$rc"

rc=0
java -jar imp.jar > /dev/null 2>&1 || rc=$?
check "no-args" "64" "$rc"

out=$(java -jar imp.jar --config "$TESTDIR/imp.conf" --dry-run "$TESTDIR/test.txt" 2>/dev/null)
check "dry-run" "would dispatch: cat $TESTDIR/test.txt" "$out"

out=$(java -jar imp.jar --config "$TESTDIR/imp.conf" "$TESTDIR/test.txt" 2>/dev/null)
check "real-dispatch" "hello" "$out"

cp "$TESTDIR/test.txt" "$TESTDIR/data.bin"
cat > "$TESTDIR/imp2.conf" <<CEOF
version=1
fallback=cat
ext.bin=printf
mime.text/plain=echo
CEOF
out=$(java -jar imp.jar --config "$TESTDIR/imp2.conf" --dry-run "$TESTDIR/data.bin" 2>/dev/null)
check "mime-wins" "would dispatch: echo $TESTDIR/data.bin" "$out"

out=$(java -jar imp.jar --config "$TESTDIR/imp.conf" --list-mappings 2>/dev/null)
check "list-mappings" "ext.txt=cat" "$out"

rc=0
java -jar imp.jar --config "$TESTDIR/imp.conf" --list-handlers --json 2>/dev/null | python3 -m json.tool > /dev/null || rc=$?
check "json-valid" "0" "$rc"

echo ""
echo "integration: $PASS passed, $FAIL failed"
[ "$FAIL" -eq 0 ]
