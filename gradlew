#!/bin/sh
# Minimal Gradle launcher for CI; delegates to the wrapper jar when present.
APP_HOME=$(cd "${0%/*}" && pwd)
exec "${JAVA_HOME:-/usr/bin}/bin/java" \
  -classpath "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" \
  org.gradle.wrapper.GradleWrapperMain "$@"
