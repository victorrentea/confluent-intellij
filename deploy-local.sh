#!/bin/zsh
# Hot-redeploys this plugin into the running IntelliJ, no restart.
# Needs -Didea.auto.reload.plugins=true in idea.vmoptions: the IDE then reloads a
# loaded plugin when one of its jars changes *content* (a new file name is ignored),
# so the version stays pinned and the jar is overwritten in place.
set -e
cd "${0:A:h}"

VERSION=262.99999.1
IDE_CONFIG=~/Library/Application\ Support/JetBrains/IntelliJIdea2026.2
JAR=confluent-intellij-$VERSION.jar
TARGET="$IDE_CONFIG/plugins/confluent-intellij/lib/$JAR"
LOG=~/Library/Logs/JetBrains/IntelliJIdea2026.2/idea.log

# buildSearchableOptions boots a headless IDE and is the slow step; a stale index is harmless
RELEASE_NAME=v$VERSION ./gradlew buildPlugin -x test -x buildSearchableOptions -q

[[ -f "$TARGET" ]] || { echo "Not installed at $TARGET — install the zip from disk once"; exit 1; }
LOG_LINES=$(wc -l < "$LOG")
cp build/libs/$JAR "$TARGET"
echo "Copied $JAR — waiting for IntelliJ to reload it (click into IntelliJ if nothing happens)…"

for i in {1..60}; do
  if tail -n +$((LOG_LINES + 1)) "$LOG" | grep -i -E 'reloaded successfully|failed to unload|Detected plugin .jar|DynamicPlugins' ; then
    exit 0
  fi
  sleep 1
done
echo "No reload line in idea.log after 60s"
exit 2
