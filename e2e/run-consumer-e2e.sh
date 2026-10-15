#!/bin/zsh
# Codex drives IntelliJ (computer use) through the consumer flow and saves screenshots to e2e/shots/.
D="${0:A:h}"
SHOTS="$D/shots/$(date +%H%M%S)"; mkdir -p "$SHOTS"
~/bin/hands-off run "Codex testează e2e consumerul din pluginul Kafka în IntelliJ: deschide tool window-ul, pornește un consumer, selectează un mesaj, face 6 screenshot-uri; ~2-3 min" -- \
  command codex exec --yolo -C "$D" "SHOTS_DIR=$SHOTS
$(cat "$D/consumer-e2e.md")" 2>&1 | tail -40
echo "SHOTS: $SHOTS"; ls "$SHOTS"
