You are an e2e UI tester driving the real IntelliJ IDEA on this Mac (main display). Loop: look at the screen, act, verify, screenshot. Be targeted: don't explore unrelated UI, don't touch other apps, don't close IntelliJ.

Context: IntelliJ has project "kafka-playground" open. A Kafka broker runs on localhost:9092. The Kafka plugin ("Kafka for Victor 🔥" tool window) has a connection named "Kafka".

Save every screenshot with `screencapture -x <path>` into SHOTS_DIR (given below), named exactly as listed.

1. Bring IntelliJ to the front: open -a "/Users/victorrentea/Applications/IntelliJ IDEA.app"
2. Open the bottom tool window "Kafka for Victor 🔥": click its icon on the LEFT tool-window stripe (a grey node-with-4-branches Kafka logo, lower half of the stripe). It shows a tab "Kafka".
   → screenshot 01-toolwindow.png
3. In that tool window, top-left toolbar: click the "Consumer" button. A new editor tab "... Consumer" opens with a "Configuration" panel on the left.
   → screenshot 02-consumer-empty.png
4. In the Configuration panel, "Topic:" combo → choose "BookAssignedToUser".
5. Check that "Start from:" shows "From the beginning" (do not change it). Click "Start Consuming". Wait ~4 s until rows appear in the messages table.
   → screenshot 03-consuming.png
6. Click the 2nd row of the messages table (the row whose Key column used to show a garbled "�" — it should now show a number). The "Details" panel on the right fills in.
   → screenshot 04-details.png
7. In Details, expand "Metadata" if collapsed (click the "Metadata" heading). Scroll the Details panel down if needed so Metadata is visible.
   → screenshot 05-metadata.png
8. Click "Stop Consuming". Leave the tab open.
9. In the tool window's left tree, click "Topics", then click the topic "BookAssignedToUser" in the right table.
   → screenshot 06-topics.png

Then report, as a short list: for each step, what you clicked and whether it matched the description. Also note anything that looked broken, cut off, misaligned, or hard to find while you used it (honest UX observations from a first-time user).
