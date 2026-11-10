#!/usr/bin/env bash
# Regenerates the Gradle wrapper jar from an installed Gradle distribution.
# Usage: source-jar from any local Gradle 8.9 install.
set -euo pipefail
cd "$(dirname "$0")/.."

OUT="gradle/wrapper/gradle-wrapper.jar"

curl -fsSL --max-time 90 \
  -o "$OUT" \
  "https://raw.githubusercontent.com/gradle/gradle/v8.9.0/gradle/wrapper/gradle-wrapper.jar" \
  || {
    echo "download failed; generate locally with a Gradle 8.9 install:" >&2
    echo "  gradle wrapper --gradle-version 8.9" >&2
    exit 1
  }

echo "wrapper jar written to $OUT"
