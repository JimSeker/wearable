package edu.cs4730.wearnotidemo;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.BigTextStyle;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.NotificationCompat.WearableExtender;
import androidx.core.app.RemoteInput;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.View;

import java.util.Map;

/**
 * This example shows varying notifications that work on the wearable device as well as on the
 * the phone.
 * <p>
 * Don't forget to use this command if running an emulator for wear.  Run everytime you connect the phone.
 * adb -d forward tcp:5601 tcp:5601
 *
 * Note, this is not working with the new wear 3 at with the emulator.  works on the older wear watchs still
 * missing something.
 */


public class MainActivity extends AppCompatActivity {
    public static String id = "test_channel_01";
    static String TAG = "MainActivity";
    int notificationID = 1;
    NotificationManager nm;
    ActivityResultLauncher<String[]> rpl;
    private final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.POST_NOTIFICATIONS};

    // Key for the string that's delivered in the action's intent
    private static final String EXTRA_VOICE_REPLY = "extra_voice_reply";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // for notifications permission now required in api 33
        //this allows us to check with multiple permissions, but in this case (currently) only need 1.
        rpl = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> isGranted) {
                    boolean granted = true;
                    for (Map.Entry<String, Boolean> x : isGranted.entrySet()) {
                        logthis(x.getKey() + " is " + x.getValue());
                        if (!x.getValue()) granted = false;
                    }
                    if (granted)
                        logthis("Permissions granted for api 33+");
                }
            }
        );


        this.findViewById(R.id.simpleButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simpleNoti();
            }
        });
        this.findViewById(R.id.addactionButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addbuttonNoti();
            }
        });
        this.findViewById(R.id.onlywearableButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onlywearableNoti();
            }
        });
        this.findViewById(R.id.bigtextButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bigTextNoti();
            }
        });
        this.findViewById(R.id.voicereplyButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                voiceReplytNoti();
            }
        });
        createchannel();
        //for the new api 33+ notifications permissions.
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!allPermissionsGranted()) {
                rpl.launch(REQUIRED_PERMISSIONS);
            }
        }
    }

    /**
     * Just a simple notification that will show up on the wearable.
     * the user can swipe the notification to the left to reveal the Open action, which invokes the intent on the handheld device.
     */
    void simpleNoti() {

        //create the intent to launch the notiactivity, then the pentingintent.
        Intent viewIntent = new Intent(this, NotiActivity.class);
        viewIntent.putExtra("NotiID", "Notification ID is " + notificationID);

        PendingIntent viewPendingIntent =
            PendingIntent.getActivity(this, 0, viewIntent, PendingIntent.FLAG_IMMUTABLE);

        //Now create the notification.  We must use the NotificationCompat or it will not work on the wearable.
        NotificationCompat.Builder notificationBuilder =
            new NotificationCompat.Builder(this, id)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Simple Noti")
                .setContentText("This is a simple notification")
                .setChannelId(id)
                .setContentIntent(viewPendingIntent);

        // Get an instance of the NotificationManager service
        NotificationManagerCompat notificationManager =
            NotificationManagerCompat.from(this);

        // Build the notification and issues it with notification manager.
        notificationManager.notify(notificationID, notificationBuilder.build());
        notificationID++;
    }

    /**
     * This one adds a button to the notification.  launches the camera for this example.
     */
    void addbuttonNoti() {
        logthis( "addbutton noti");
        //create the intent to launch the notiactivity, then the pentingintent.
        Intent viewIntent = new Intent(this, NotiActivity.class);
        viewIntent.putExtra("NotiID", "Notification ID is " + notificationID);

        PendingIntent viewPendingIntent =
            PendingIntent.getActivity(this, 0, viewIntent, PendingIntent.FLAG_IMMUTABLE);

        // we are going to add an intent to open the camera here.
        //Intent cameraIntent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        PendingIntent cameraPendingIntent =
            PendingIntent.getActivity(this, 0, cameraIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Action.WearableExtender inlineActionForWear2 =
            new NotificationCompat.Action.WearableExtender()
                .setHintDisplayActionInline(true)
                .setHintLaunchesActivity(true);

        // Add an action to allow replies.
        NotificationCompat.Action pictureAction =
            new NotificationCompat.Action.Builder(
                R.drawable.ic_action_time,
                "Open Camera",
                cameraPendingIntent)
                .extend(inlineActionForWear2)
                .build();

        //Now create the notification.  We must use the NotificationCompat or it will not work on the wearable.
        NotificationCompat.Builder notificationBuilder =
            new NotificationCompat.Builder(this, id)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("add button Noti")
                .setContentText("Tap for full message.")
                .setContentIntent(viewPendingIntent)
                .setChannelId(id)
                .addAction(pictureAction);

        // Get an instance of the NotificationManager service
        NotificationManagerCompat notificationManager =
            NotificationManagerCompat.from(this);

        // Build the notification and issues it with notification manager.
        notificationManager.notify(notificationID, notificationBuilder.build());
        notificationID++;

    }

    /**
     * Both the phone and wear will have a notification.  This adds the button so it only shows
     * on the wearable device and not the phone notification.
     */
    void onlywearableNoti() {
        //create the intent to launch the notiactivity, then the pentingintent.
        Intent viewIntent = new Intent(this, NotiActivity.class);
        viewIntent.putExtra("NotiID", "Notification ID is " + notificationID);

        PendingIntent viewPendingIntent =
            PendingIntent.getActivity(this, 0, viewIntent, PendingIntent.FLAG_IMMUTABLE);

        // we are going to add an intent to open the camera here.
        Intent cameraIntent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
        PendingIntent cameraPendingIntent =
            PendingIntent.getActivity(this, 0, cameraIntent, PendingIntent.FLAG_IMMUTABLE);

        // Create the action
        NotificationCompat.Action action =
            new NotificationCompat.Action.Builder(R.drawable.ic_action_time,
                "Open Camera", cameraPendingIntent)
                .build();


        //Now create the notification.  We must use the NotificationCompat or it will not work on the wearable.
        NotificationCompat.Builder notificationBuilder =
            new NotificationCompat.Builder(this, id)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Button on Wear Only")
                .setContentText("tap to open message")
                .setContentIntent(viewPendingIntent)
                .setChannelId(id)
                .extend(new WearableExtender().addAction(action));

        // Get an instance of the NotificationManager service
        NotificationManagerCompat notificationManager =
            NotificationManagerCompat.from(this);

        // Build the notification and issues it with notification manager.
        notificationManager.notify(notificationID, notificationBuilder.build());
        notificationID++;
    }

    /**
     * using the bigtext notification.
     */
    void bigTextNoti() {
        //create the intent to launch the notiactivity, then the pentingintent.
        Intent viewIntent = new Intent(this, NotiActivity.class);
        viewIntent.putExtra("NotiID", "Notification ID is " + notificationID);

        PendingIntent viewPendingIntent =
            PendingIntent.getActivity(this, 0, viewIntent, PendingIntent.FLAG_IMMUTABLE);

        BigTextStyle bigStyle = new NotificationCompat.BigTextStyle();
        bigStyle.bigText("Big text style.\n"
            + "We should have more room to add text for the user to read, instead of a short message.");


        //Now create the notification.  We must use the NotificationCompat or it will not work on the wearable.
        NotificationCompat.Builder notificationBuilder =
            new NotificationCompat.Builder(this, id)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Simple Noti")
                .setContentText("This is a simple notification")
                .setContentIntent(viewPendingIntent)
                .setChannelId(id)
                .setStyle(bigStyle);

        // Get an instance of the NotificationManager service
        NotificationManagerCompat notificationManager =
            NotificationManagerCompat.from(this);

        // Build the notification and issues it with notification manager.
        notificationManager.notify(notificationID, notificationBuilder.build());
        notificationID++;
    }


    /**
     * This adds the voice response for the wearable device.
     * It comes back via an intent, which is shown in voiceNotiActivity.
     */
    void voiceReplytNoti() {


        //create the intent to launch the notiactivity, then the pentingintent.
        Intent replyIntent = new Intent(this, VoiceNotiActivity.class);
        replyIntent.putExtra("NotiID", "Notification ID is " + notificationID);

        PendingIntent replyPendingIntent =
            PendingIntent.getActivity(this, 0, replyIntent, PendingIntent.FLAG_MUTABLE);

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
            new NotificationCompat.Builder(this, id)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("reply Noti")
                .setContentText("voice reply example.")
                .setChannelId(id)
                .extend(new WearableExtender().addAction(action));

        // Get an instance of the NotificationManager service
        NotificationManagerCompat notificationManager =
            NotificationManagerCompat.from(this);

        // Build the notification and issues it with notification manager.
        notificationManager.notify(notificationID, notificationBuilder.build());
        notificationID++;

    }


    /**
     * for API 26+ create notification channels
     */
    private void createchannel() {
        NotificationChannel mChannel = new NotificationChannel(id,
            getString(R.string.channel_name),  //name of the channel
            NotificationManager.IMPORTANCE_DEFAULT);   //importance level
        //important level: default is is high on the phone.  high is urgent on the phone.  low is medium, so none is low?
        // Configure the notification channel.
        mChannel.setDescription(getString(R.string.channel_description));
        mChannel.enableLights(true);
        //Sets the notification light color for notifications posted to this channel, if the device supports this feature.
        mChannel.setLightColor(Color.RED);
        mChannel.enableVibration(true);
        mChannel.setShowBadge(true);
        mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

        Log.d(TAG, "channels created.");
    }
    //ask for permissions when we start.
    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
    public void logthis(String msg) {
        //logger.append(msg);
        Log.d(TAG, msg);
    }
}
