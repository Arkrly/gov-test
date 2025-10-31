#!/bin/sh
# ----------------------------------------------------------------------------
# Maven Wrapper Startup Script
# ----------------------------------------------------------------------------

if [ -z "$JAVA_HOME" ]; then
  JAVA_EXE="$(command -v java)"
  if [ -z "$JAVA_EXE" ]; then
    echo "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH." >&2
    exit 1
  fi
else
  JAVA_EXE="$JAVA_HOME/bin/java"
fi

WRAPPER_DIR="$(cd "$(dirname "$0")"; pwd -P)/.mvn/wrapper"
WRAPPER_JAR="$WRAPPER_DIR/maven-wrapper.jar"
WRAPPER_PROPERTIES="$WRAPPER_DIR/maven-wrapper.properties"

if [ ! -f "$WRAPPER_JAR" ]; then
  . "$WRAPPER_DIR/maven-wrapper-download.sh" "$WRAPPER_DIR" "$WRAPPER_PROPERTIES" "$WRAPPER_JAR" || exit 1
fi

exec "$JAVA_EXE" -Dmaven.multiModuleProjectDirectory="${MAVEN_PROJECTBASEDIR:-$PWD}" \
  -cp "$WRAPPER_JAR" org.apache.maven.wrapper.MavenWrapperMain "$@"
