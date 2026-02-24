#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
REQUIRED_MAVEN_VERSION="3.9.12"

cd "$ROOT_DIR"

CURRENT_MAVEN_VERSION="$(mvn -v | awk 'NR==1 {print $3}')"

if [[ "$CURRENT_MAVEN_VERSION" != "$REQUIRED_MAVEN_VERSION" ]]; then
  echo "Maven ${REQUIRED_MAVEN_VERSION} is required. Current version: ${CURRENT_MAVEN_VERSION}" >&2
  exit 1
fi

mvn -q -DskipTests compile
mvn -q \
  -DskipTests \
  -Dwarehouse.root="$ROOT_DIR" \
  -Dexec.mainClass=com.greateastern.warehouse.migration.MigrationRunner \
  -Dexec.classpathScope=runtime \
  org.codehaus.mojo:exec-maven-plugin:3.5.0:java
