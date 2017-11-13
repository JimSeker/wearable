Android wearable Examples
========

eclipse/ is old examples in the eclipse format for wear v1.0.0 

v1.3.0/ are some of these examples halted at v1.3.0, since v2.1 and support.wear changes/deprecated the APIs.

<b>WearNotiDemo</b> example of how to send notifications to the wearable device.  Should work on all versions of wear 1.0+ and 2.0.0

<b>myWatchFace</b> several examples of watch faces for the wear device. uses wear 2.1  api 21+

<b>TapEventsDemo</b> a simple watchface to demo the tapevents.  uses wear 2.1  api 21+

<b> All examples below are using Wear 2.1+ with support.wear 27+   This requires the min API of 23 for all the wear examples</b>

<b>WearApp</b> is an example of standalone app for the watch. It has a button to show a random number.  

<b>WearSensorsDemo</b> is an example of using the sensor (Game Rotation vector).

<b>WearableDataLayer</b> is an example of how send messages back and forth between the wear app and a phone app using the data layer in google-playservices.  You will need to install the wear app on to the wear device.  Install the mobile app on the phone/tablet device that is connected to the wear device.  A note, as far as I can tell, the GoogleApiClient is not deprecated in wear and that is the only place it isn't deprecated, they may change in new playservice wearapi's (after 11.6.0) update.  

<b>WearAppVoice</b> is a simple app to show how the voice recognizer works. Click the button to start the demo.   The App-provided Voice actions fails, since google has so badly documented it.   "Start Voice App" should work, but instead use "Open Voice App", after the ok google command.  http://developer.android.com/training/wearables/apps/voice.html 


These are example code for University of Wyoming, Cosc 4730 Mobile Programming course. All examples are for Android.
