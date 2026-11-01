#!/usr/bin/env bash
set -euo pipefail
rm -rf out
mkdir -p out
find src/main/java -name '*.java' -print0 | xargs -0 javac --release 17 -d out
javac --release 17 -cp out -d out tools/com/arnur/transport/tools/SmokeTest.java
java -cp out com.arnur.transport.tools.SmokeTest
