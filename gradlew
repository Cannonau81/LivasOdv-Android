#!/usr/bin/env sh
# GitHub-friendly Gradle bootstrap. The CI workflow installs Gradle 8.13 directly.
# Locally, use this script when Gradle is already on PATH.
set -eu
if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
fi
echo "Gradle non trovato. Su GitHub usa .github/workflows/android-ci.yml (Gradle 8.13 viene installato automaticamente)." >&2
exit 1
