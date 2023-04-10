package edu.cs4730.wearabledatalayer;

import android.content.Intent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/*
  *  This is a listener on the mobile device to get messages via
  *  the datalayer and then pass it to the main activity so it can be
  *  displayed.   the messages should be coming from the device/phone.
 */
public class ListenerService extends WearableListenerService {
    String TAG = "wear listener";
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        if (messageEvent.getPath().equals("/message_path")) {
            final String message = new String(messageEvent.getData());
            Log.v(TAG, "Message path received is: " + messageEvent.getPath());
            Log.v(TAG, "Message received is: " + message);

            // Broadcast message to wearable activity for display
            Intent messageIntent = new Intent();
            messageIntent.setAction(Intent.ACTION_SEND);
            messageIntent.putExtra("message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
        }
        else {
            super.onMessageReceived(messageEvent);
        }
    }

}
