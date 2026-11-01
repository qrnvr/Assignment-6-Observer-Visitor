#!/usr/bin/env bash
set -euo pipefail
rm -rf out
mkdir -p out
find src/main/java -name '*.java' -print0 | xargs -0 javac --release 17 -d out
java -cp out com.arnur.transport.app.TransportApplication "${1:-3}"
