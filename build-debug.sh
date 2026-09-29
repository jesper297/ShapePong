#!/usr/bin/env sh
set -eu

ROOT=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)

command -v java >/dev/null 2>&1 || {
  echo "Fel: Java saknas. Installera JDK 17 eller Android Studio." >&2
  exit 1
}

JAVA_MAJOR=$(java -version 2>&1 | sed -n '1s/.*version "\([0-9]*\).*/\1/p')
[ "$JAVA_MAJOR" = "17" ] || {
  echo "Fel: JDK 17 krävs, hittade version ${JAVA_MAJOR:-okänd}." >&2
  exit 1
}

if [ -z "${ANDROID_HOME:-}" ] && [ -z "${ANDROID_SDK_ROOT:-}" ]; then
  echo "Fel: Android SDK saknas. Installera Android Studio och SDK Platform 35." >&2
  echo "Sätt sedan ANDROID_HOME till SDK-mappen." >&2
  exit 1
fi

"$ROOT/gradlew" :app:assembleDebug
echo "APK klar: $ROOT/app/build/outputs/apk/debug/app-debug.apk"
