package edu.cs4730.wearabledatalayer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

/*
 * Wear Device code  so I can kept this straight.
 *
 * This will receive messages (from a device/phone) via the datalayer (through the listener code)
 * and display them to the wear device.  There is also a button to send a message
 * to the device/phone as well.
 *
 * if the wear device receives a message from the phone/device it will then send a message back
 * via the button on the wear device, it can also send a message to the device/phone as well.
 *    There is no auto response from the phone/device otherwise we would get caught in a loop!
 */


public class MainActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    GoogleApiClient googleClient;
    private final static String TAG = "Wear MainActivity";
    private TextView mTextView;
    Button myButton;
    int num = 1;
    boolean connected = false;
    String datapath = "/message_path";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = (TextView) findViewById(R.id.text);
        //send a message from the wear.  This one will not have response.
        myButton = (Button) findViewById(R.id.wrbutton);
        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "click Hello device " + num;
                //Requires a new thread to avoid blocking the UI
                new SendThread(datapath, message).start();
                num++;
            }
        });
        // Register the local broadcast receiver to receive messages from the listener.
        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        MessageReceiver messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);

        // Build a new GoogleApiClient that includes the Wearable API
        googleClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            Log.v(TAG, "Main activity received message: " + message);
            // Display message in UI
            mTextView.setText(message);
            //here, send a message back.
            message = "Hello device " + num;
            //Requires a new thread to avoid blocking the UI
            new SendThread(datapath, message).start();
            num++;
        }
    }

    // Connect to the data layer when the Activity starts
    @Override
    protected void onStart() {
        super.onStart();
        googleClient.connect();
    }


    //needed to send a message back to the device.  hopefully.
    @Override
    public void onConnected(Bundle bundle) {
        connected = true;
    }

    @Override
    public void onConnectionSuspended(int i) {
        connected = false;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        connected = false;
    }

    //This actually sends the message to the wearable device.
    class SendThread extends Thread {
        String path;
        String message;

        //constructor
        SendThread(String p, String msg) {
            path = p;
            message = msg;
        }

        //sends the message via the thread.  this will send to all wearables connected, but
        //since there is (should only?) be one, so no problem.
        public void run() {
            NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(googleClient).await();
            for (Node node : nodes.getNodes()) {
                MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(googleClient, node.getId(), path, message.getBytes()).await();
                if (result.getStatus().isSuccess()) {
                    Log.v(TAG, "SendThread: message send to " + node.getDisplayName());

                } else {
                    // Log an error
                    Log.v(TAG, "SendThread: message failed to" + node.getDisplayName());
                }
            }
        }
    }
}
