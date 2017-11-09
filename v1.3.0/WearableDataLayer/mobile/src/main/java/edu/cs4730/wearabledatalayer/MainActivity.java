package edu.cs4730.wearabledatalayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.os.Handler;
import android.os.Message;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

/*
 * this is the main activity on the device/phone  (just to kept everything straight)
 *
 * The device will setup a listener to receive messages from the wear device and display them to the screen.
 *
 * It also setups up a button, so it can send a message to the wear device, the wear device will auto
 * response to the message.   This code does not auto response, otherwise we would get caught in a loop.
 *
 * This is all down via the datalayer in the googleApiClient that requires om.google.android.gms:play-services-wearable
 * in the gradle (both wear and mobile).  Also the applicationId MUST be the same in both files as well
 * both use a the "/message_path" to send/receive messages.
 *
 */

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    GoogleApiClient googleClient;
    String datapath = "/message_path";
    Button mybutton;
    TextView logger;
    protected Handler handler;
    String TAG = "Mobile MainActivity";
    int num =1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get the widgets
        mybutton = (Button) findViewById(R.id.sendbtn);
        mybutton.setEnabled(false);  //disable until we are connected.
        mybutton.setOnClickListener(this);
        logger = (TextView) findViewById(R.id.logger);

        // Build a new GoogleApiClient that includes the Wearable API
        googleClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        //once everything is connected, we should be able to send a message.  handled in onConnect.

        //message handler for the send thread.
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Bundle stuff = msg.getData();
                logthis(stuff.getString("logthis"));
                return true;
            }
        });

        // Register the local broadcast receiver
        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        MessageReceiver messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);

    }

    /*
    * simple method to add the log TextView.
    */
    public void logthis(String newinfo) {
        if (newinfo.compareTo("") != 0) {
            logger.append("\n" + newinfo);
        }
    }
    //setup a broadcast receiver to receive the messages from the wear device via the listenerService.
    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            Log.v(TAG, "Main activity received message: " + message);
            // Display message in UI
           logthis(message);

        }
    }


    // Connect to the data layer when the Activity starts
    @Override
    protected void onStart() {
        super.onStart();
        googleClient.connect();
    }

    // Send a message when the data layer connection is successful.
    @Override
    public void onConnected(Bundle bundle) {
        logthis("connected to wear device");
        mybutton.setEnabled(true);
    }

    // Disconnect from the data layer when the Activity stops
    @Override
    protected void onStop() {
        if (null != googleClient && googleClient.isConnected()) {
            googleClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnectionSuspended(int i) {
        logthis("connection suspended");
        mybutton.setEnabled(false);  //don't allow message to be sent.
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        logthis("Connection failed.");
        mybutton.setEnabled(false); //don't allow message to be sent.
    }

    //button listener
    @Override
    public void onClick(View v) {
        String message = "Hello wearable " + num;
        //Requires a new thread to avoid blocking the UI
        new SendThread(datapath, message).start();
        num ++;
    }

    //method to create up a bundle to send to a handler via the thread below.
    public void sendmessage(String logthis) {
        Bundle b = new Bundle();
        b.putString("logthis", logthis);
        Message msg = handler.obtainMessage();
        msg.setData(b);
        msg.arg1 = 1;
        msg.what = 1; //so the empty message is not used!
        handler.sendMessage(msg);

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
        //since there is (should only?) be one, no problem.
        public void run() {
            NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(googleClient).await();
            for (Node node : nodes.getNodes()) {
                MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(googleClient, node.getId(), path, message.getBytes()).await();
                if (result.getStatus().isSuccess()) {
                    sendmessage("SendThread: message send to " + node.getDisplayName());
                    Log.v(TAG, "SendThread: message send to "+ node.getDisplayName());

                } else {
                    // Log an error
                    sendmessage("SendThread: message failed to" + node.getDisplayName());
                    Log.v(TAG, "SendThread: message failed to" + node.getDisplayName());
                }
            }
        }
    }
}
