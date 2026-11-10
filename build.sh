#!/bin/bash
export JAVA_HOME=/data/data/com.termux/files/usr/lib/jvm/java-21-openjdk
export ANDROID_HOME=/data/data/com.termux/files/home/android-sdk
export PATH="$JAVA_HOME/bin:$PATH"
cd /data/data/com.termux/files/home/Clevemusic
exec /data/data/com.termux/files/home/cleve/tools/gradle-8.9/bin/gradle "$@"
