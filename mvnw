#!/bin/sh
# Maven Wrapper script — downloads Maven if not present, then runs it.
# Based on Apache Maven Wrapper 3.3.4

set -e

# Determine project base directory
MAVEN_PROJECTBASEDIR="${MAVEN_BASEDIR:-$(cd "$(dirname "$0")" && pwd)}"
MAVEN_WRAPPER_PROPERTIES="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties"

if [ -f "$MAVEN_WRAPPER_PROPERTIES" ]; then
    MAVEN_DIST_URL=$(grep 'distributionUrl' "$MAVEN_WRAPPER_PROPERTIES" | cut -d'=' -f2- | tr -d '\r')
fi

MAVEN_DIST_URL="${MAVEN_DIST_URL:-https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.16/apache-maven-3.9.16-bin.zip}"
MAVEN_HOME="$HOME/.m2/wrapper/dists/$(basename "$MAVEN_DIST_URL" .zip)"

if [ ! -d "$MAVEN_HOME" ]; then
    echo "Downloading Maven from $MAVEN_DIST_URL"
    mkdir -p "$MAVEN_HOME"
    TMPFILE=$(mktemp)
    if command -v curl > /dev/null 2>&1; then
        curl -fsSL -o "$TMPFILE" "$MAVEN_DIST_URL"
    elif command -v wget > /dev/null 2>&1; then
        wget -q -O "$TMPFILE" "$MAVEN_DIST_URL"
    else
        echo "Error: curl or wget required" >&2
        exit 1
    fi
    unzip -q -o "$TMPFILE" -d "$MAVEN_HOME"
    rm -f "$TMPFILE"
fi

MAVEN_BIN=$(find "$MAVEN_HOME" -name mvn -path '*/bin/mvn' | head -1)

if [ -z "$MAVEN_BIN" ]; then
    echo "Error: Could not find mvn binary in $MAVEN_HOME" >&2
    exit 1
fi

exec "$MAVEN_BIN" "$@"
