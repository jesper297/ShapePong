#!/usr/bin/env sh
set -eu

GRADLE_VERSION=8.9
BASE_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
CACHE_DIR="${GRADLE_USER_HOME:-$HOME/.gradle}/wrapper/dists/gradle-$GRADLE_VERSION-bin/local"
GRADLE_BIN="$CACHE_DIR/gradle-$GRADLE_VERSION/bin/gradle"

if [ ! -x "$GRADLE_BIN" ]; then
  command -v java >/dev/null 2>&1 || { echo "Java 17 saknas." >&2; exit 1; }
  command -v curl >/dev/null 2>&1 || { echo "curl saknas." >&2; exit 1; }
  command -v unzip >/dev/null 2>&1 || { echo "unzip saknas." >&2; exit 1; }
  mkdir -p "$CACHE_DIR"
  ARCHIVE="$CACHE_DIR/gradle-$GRADLE_VERSION-bin.zip"
  echo "Hämtar Gradle $GRADLE_VERSION …"
  curl --fail --location --retry 3 --output "$ARCHIVE" \
    "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip"
  unzip -q "$ARCHIVE" -d "$CACHE_DIR"
  rm -f "$ARCHIVE"
fi

exec "$GRADLE_BIN" -p "$BASE_DIR" "$@"
