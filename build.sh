#!/bin/bash
# Local Termux build helper: points Gradle at the JDK + ARM64 aapt2 available here.
# CI does not use this file.
set -eu

export JAVA_HOME=/data/data/com.termux/files/usr/lib/jvm/java-21-openjdk
export ANDROID_HOME=/data/data/com.termux/files/home/android-sdk
export PATH="$JAVA_HOME/bin:$PATH"

cd "$(dirname "$0")"

exec /data/data/com.termux/files/home/cleve/tools/gradle-8.9/bin/gradle \
  -I gradle.termux.properties \
  "$@"
