android-command-node
====================

This project is to develop an Android app that works like a voice command agent that interacts with a server that is configured for your home. The final goal is to have an android app that constantly listens for a keyword (Computer, Android, Home, etc) and when that keyword is detected, turns on voice recognition. A command such as "Lights in Living Room On" is sent to a server configured to interact with all the networked devices in your home. It will parse the command and do some action, then return a JSON response to the client, which will react in a way determined by the return JSON. It should be able to support spoken, text, audio, video, and web responses, if available from the action performed.
