package edu.cs4730.wearnotidemo;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.BigTextStyle;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.NotificationCompat.WearableExtender;
import android.support.v4.app.RemoteInput;
import android.util.Log;
import android.view.View;

/*
 * This example shows varying notifications that work on the wearable device as well as on the 
 * the phone.  
 * 
 * 
 * Don't forget to use this command if running an emulator for wear.  Run everytime you connect the phone.
 * adb -d forward tcp:5601 tcp:5601
 */


public class MainActivity extends Activity {

	int notificationID = 1;
	// Key for the string that's delivered in the action's intent
	private static final String EXTRA_VOICE_REPLY = "extra_voice_reply";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		this.findViewById(R.id.simpleButton).setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				simpleNoti();
			}
		});
		this.findViewById(R.id.addactionButton).setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				addbuttonNoti();
			}
		});
		this.findViewById(R.id.onlywearableButton).setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				onlywearableNoti();
			}
		});
		this.findViewById(R.id.bigtextButton).setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				bigTextNoti();
			}
		});
		this.findViewById(R.id.voicereplyButton).setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				voiceReplytNoti();
			}
		});
	}
	
	/*
	 * Just a simple notification that will show up on the wearable.
	 * the user can swipe the notification to the left to reveal the Open action, which invokes the intent on the handheld device.
	 */
	void simpleNoti() {
		
		//create the intent to launch the notiactivity, then the pentingintent.
		Intent viewIntent = new Intent(this, NotiActivity.class);
		viewIntent.putExtra("NotiID", "Notification ID is " + notificationID);
		
		PendingIntent viewPendingIntent =
		        PendingIntent.getActivity(this, 0, viewIntent, 0);

		//Now create the notification.  We must use the NotificationCompat or it will not work on the wearable.
		NotificationCompat.Builder notificationBuilder =
		        new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.ic_launcher)
		        .setContentTitle("Simple Noti")
		        .setContentText("This is a simple notification")
		        .setContentIntent(viewPendingIntent);

		// Get an instance of the NotificationManager service
		NotificationManagerCompat notificationManager =
		        NotificationManagerCompat.from(this);

		// Build the notification and issues it with notification manager.
		notificationManager.notify(notificationID, notificationBuilder.build());
		notificationID++;
	}

	/*
	 * This one adds a button to the notification.  lauches the camera for this example.
	 */
	void addbuttonNoti() {
		Log.i("main", "addbutton noti");
		//create the intent to launch the notiactivity, then the pentingintent.
		Intent viewIntent = new Intent(this, NotiActivity.class);
		viewIntent.putExtra("NotiID", "Notification ID is " + notificationID);
		
		PendingIntent viewPendingIntent =
		        PendingIntent.getActivity(this, 0, viewIntent, 0);
		
		// we are going to add an intent to open the camera here.
		Intent cameraIntent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
		PendingIntent cameraPendingIntent =
		        PendingIntent.getActivity(this, 0, cameraIntent, 0);
		

		//Now create the notification.  We must use the NotificationCompat or it will not work on the wearable.
		NotificationCompat.Builder notificationBuilder =
		        new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.ic_launcher)
		        .setContentTitle("add button Noti")
		        .setContentText("swipe left to open camera.")
		        .setContentIntent(viewPendingIntent)
		        .addAction(R.drawable.ic_action_time,
                "take Picutre", cameraPendingIntent);

		// Get an instance of the NotificationManager service
		NotificationManagerCompat notificationManager =
		        NotificationManagerCompat.from(this);

		// Build the notification and issues it with notification manager.
		notificationManager.notify(notificationID, notificationBuilder.build());
		notificationID++;
		
	}
	
	/*
	 * This adds the button so it only shows on the wearable device and not
	 * the phone notification.
	 */
	void onlywearableNoti() {
		//create the intent to launch the notiactivity, then the pentingintent.
		Intent viewIntent = new Intent(this, NotiActivity.class);
		viewIntent.putExtra("NotiID", "Notification ID is " + notificationID);
		
		PendingIntent viewPendingIntent =
		        PendingIntent.getActivity(this, 0, viewIntent, 0);
		
		// we are going to add an intent to open the camera here.
		Intent cameraIntent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
		PendingIntent cameraPendingIntent =
		        PendingIntent.getActivity(this, 0, cameraIntent, 0);
		
		// Create the action
		NotificationCompat.Action action =
		        new NotificationCompat.Action.Builder(R.drawable.ic_action_time,
		                "take a Picutre", cameraPendingIntent)
		                .build();


		//Now create the notification.  We must use the NotificationCompat or it will not work on the wearable.
		NotificationCompat.Builder notificationBuilder =
		        new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.ic_launcher)
		        .setContentTitle("add button Noti")
		        .setContentText("swipe left to open camera.")
		        .setContentIntent(viewPendingIntent)
		        .extend(new WearableExtender().addAction(action));

		// Get an instance of the NotificationManager service
		NotificationManagerCompat notificationManager =
		        NotificationManagerCompat.from(this);

		// Build the notification and issues it with notification manager.
		notificationManager.notify(notificationID, notificationBuilder.build());
		notificationID++;
	}
	
	/*
	 * using the bigtext notificiation.
	 */
	void bigTextNoti() {
		//create the intent to launch the notiactivity, then the pentingintent.
		Intent viewIntent = new Intent(this, NotiActivity.class);
		viewIntent.putExtra("NotiID", "Notification ID is " + notificationID);
		
		PendingIntent viewPendingIntent =
		        PendingIntent.getActivity(this, 0, viewIntent, 0);

		BigTextStyle bigStyle = new NotificationCompat.BigTextStyle();
		bigStyle.bigText("Big text style.\n"
				+ "We should have more room to add text for the user to read, instead of a short message.");

		
		//Now create the notification.  We must use the NotificationCompat or it will not work on the wearable.
		NotificationCompat.Builder notificationBuilder =
		        new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.ic_launcher)
		        .setContentTitle("Simple Noti")
		        .setContentText("This is a simple notification")
		        .setContentIntent(viewPendingIntent)
		        .setStyle(bigStyle);

		// Get an instance of the NotificationManager service
		NotificationManagerCompat notificationManager =
		        NotificationManagerCompat.from(this);

		// Build the notification and issues it with notification manager.
		notificationManager.notify(notificationID, notificationBuilder.build());
		notificationID++;
	}
	
	
	/*
	 * This adds the voice response for the wearable device.
	 * It comes back via an intent, which is shown in voiceNotiActivity.
	 */
	void voiceReplytNoti() {
		

		//create the intent to launch the notiactivity, then the pentingintent.
		Intent replyIntent = new Intent(this, VoiceNotiActivity.class);
		replyIntent.putExtra("NotiID", "Notification ID is " + notificationID);
		
		PendingIntent replyPendingIntent =
		        PendingIntent.getActivity(this, 0, replyIntent, 0);
		
		// create the remote input part for the notification.
		RemoteInput remoteInput = new RemoteInput.Builder(EXTRA_VOICE_REPLY)
        .setLabel("Reply")
        .build();
		
		
		// Create the reply action and add the remote input
		NotificationCompat.Action action =
		        new NotificationCompat.Action.Builder(R.drawable.ic_action_map,
		                "Reply", replyPendingIntent)
		                .addRemoteInput(remoteInput)
		                .build();



		//Now create the notification.  We must use the NotificationCompat or it will not work on the wearable.
		NotificationCompat.Builder notificationBuilder =
		        new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.ic_launcher)
		        .setContentTitle("reply Noti")
		        .setContentText("voice reply example.")
		        .extend(new WearableExtender().addAction(action));

		// Get an instance of the NotificationManager service
		NotificationManagerCompat notificationManager =
		        NotificationManagerCompat.from(this);

		// Build the notification and issues it with notification manager.
		notificationManager.notify(notificationID, notificationBuilder.build());
		notificationID++;
		
	}
}
