#!/bin/bash
# Local Termux build helper.
# CI builds do not use this file — it only supplies the host-specific pieces that a
# Termux environment needs (JDK path and a native ARM64 aapt2).
set -eu

export JAVA_HOME=/data/data/com.termux/files/usr/lib/jvm/java-21-openjdk
export ANDROID_HOME=/data/data/com.termux/files/home/android-sdk
export PATH="$JAVA_HOME/bin:$PATH"

cd "$(dirname "$0")"

exec /data/data/com.termux/files/home/cleve/tools/gradle-8.9/bin/gradle \
  -Dorg.gradle.project.android.aapt2FromMavenOverride=/data/data/com.termux/files/usr/bin/aapt2 \
  "$@"
