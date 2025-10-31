#!/bin/sh
set -euo pipefail

WRAPPER_DIR=$1
PROPERTIES_FILE=$2
JAR_FILE=$3

if [ ! -f "$PROPERTIES_FILE" ]; then
  echo "Cannot find $PROPERTIES_FILE" >&2
  exit 1
fi

WRAPPER_URL=$(grep -E "^wrapperUrl" "$PROPERTIES_FILE" | cut -d'=' -f2-)
if [ -z "$WRAPPER_URL" ]; then
  echo "wrapperUrl is not set in $PROPERTIES_FILE" >&2
  exit 1
fi

mkdir -p "$WRAPPER_DIR"
TMP_JAR="$WRAPPER_DIR/maven-wrapper.jar.part"

if command -v curl >/dev/null 2>&1; then
  curl -fsSL "$WRAPPER_URL" -o "$TMP_JAR"
elif command -v wget >/dev/null 2>&1; then
  wget -q -O "$TMP_JAR" "$WRAPPER_URL"
else
  echo "ERROR: curl or wget is required to download the Maven wrapper." >&2
  exit 1
fi

mv "$TMP_JAR" "$JAR_FILE"
