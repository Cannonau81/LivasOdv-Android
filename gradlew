#!/usr/bin/env sh
set -eu

GRADLE_VERSION="8.13"
BASE_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
BOOTSTRAP_DIR="${GRADLE_BOOTSTRAP_DIR:-$BASE_DIR/.gradle-bootstrap}"
GRADLE_HOME="$BOOTSTRAP_DIR/gradle-$GRADLE_VERSION"
GRADLE_BIN="$GRADLE_HOME/bin/gradle"
ZIP="$BOOTSTRAP_DIR/gradle-$GRADLE_VERSION-bin.zip"
URL="https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip"

if [ ! -x "$GRADLE_BIN" ]; then
  mkdir -p "$BOOTSTRAP_DIR"
  echo "Downloading Gradle $GRADLE_VERSION..." >&2
  if command -v curl >/dev/null 2>&1; then
    curl --fail --location --retry 5 --retry-delay 5 --output "$ZIP" "$URL"
  elif command -v wget >/dev/null 2>&1; then
    wget -O "$ZIP" "$URL"
  else
    echo "curl or wget is required to bootstrap Gradle $GRADLE_VERSION." >&2
    exit 1
  fi
  rm -rf "$GRADLE_HOME"
  unzip -q "$ZIP" -d "$BOOTSTRAP_DIR"
fi

exec "$GRADLE_BIN" "$@"
