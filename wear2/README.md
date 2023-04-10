Android wearable Examples for Wear 2.0
========


` All examples below are using Wear 2.3+ with support.wear 27+.  This requires the min API of 23 for all the wear examples  Note, they were all tested and working on Wear OS 1.0 device (wear v2.3.0 and Android OS 8.0.0)`

`myWatchFace` several examples of watch faces for the wear device. Can be degraded to API 21 and should still work.

`TapEventsDemo` a simple watchface to demo the tapevents.  Can be degraded to API 21 and should still work.

`WearApp` is an example of standalone app for the watch. It has a button to show a random number.  updated to wear 3.0

`WearSensorsDemo` is an example of using the sensor (Game Rotation vector).

`WearableDataLayer` is an example of how send messages back and forth between the wear app and a phone app using the data layer in google-playservices.  You will need to install the wear app on to the wear device.  Install the mobile app on the phone/tablet device that is connected to the wear device.  Uses the listener service in a separate class.  Uses send messages (not send data).

`WearableDataLayer2` is an example of how send messages back and forth between the wear app and a phone app using the data layer in google-playservices.  You will need to install the wear app on to the wear device.  Install the mobile app on the phone/tablet device that is connected to the wear device.  The listeners for the data are in the mainactivity. Uses send messages (not send data).

`WearableDataLayer3` is an example of how send data back and forth between the wear app and a phone app using the data layer in google-playservices.  You will need to install the wear app on to the wear device.  Install the mobile app on the phone/tablet device that is connected to the wear device.  It sends via the data commands, which is a broadcast, instead of the send message command.

`WearAppVoice` is a simple app to show how the voice recognizer works. Click the button to start the demo.   The App-provided Voice actions fails, since Google has so badly documented it.   "Start Voice App" should work, but instead use "Open Voice App", after the ok Google command.  http://developer.android.com/training/wearables/apps/voice.html 


---

These are example code for University of Wyoming, Cosc 4730 Mobile Programming course and cosc 4735 Advance Mobile Programing course. 
All examples are for Android.
