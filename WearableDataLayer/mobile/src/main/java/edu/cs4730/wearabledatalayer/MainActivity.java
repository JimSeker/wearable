package edu.cs4730.wearabledatalayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.os.Handler;
import android.os.Message;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
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
 * debuging over bluetooth.
 * https://developer.android.com/training/wearables/apps/debugging.html
 */

public class MainActivity extends AppCompatActivity implements
    View.OnClickListener {

    String datapath = "/message_path";
    Button mybutton;
    TextView logger;
    protected Handler handler;
    String TAG = "Mobile MainActivity";
    int num = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get the widgets
        mybutton = findViewById(R.id.sendbtn);
        mybutton.setOnClickListener(this);
        logger = findViewById(R.id.logger);

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

    //button listener
    @Override
    public void onClick(View v) {
        String message = "Hello wearable " + num;
        //Requires a new thread to avoid blocking the UI
        new SendThread(datapath, message).start();
        num++;
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

            //first get all the nodes, ie connected wearable devices.
            Task<List<Node>> nodeListTask =
                Wearable.getNodeClient(getApplicationContext()).getConnectedNodes();
            try {
                // Block on a task and get the result synchronously (because this is on a background
                // thread).
                List<Node> nodes = Tasks.await(nodeListTask);

                //Now send the message to each device.
                for (Node node : nodes) {
                    Task<Integer> sendMessageTask =
                        Wearable.getMessageClient(MainActivity.this).sendMessage(node.getId(), path, message.getBytes());

                    try {
                        // Block on a task and get the result synchronously (because this is on a background
                        // thread).
                        Integer result = Tasks.await(sendMessageTask);
                        sendmessage("SendThread: message send to " + node.getDisplayName());
                        Log.v(TAG, "SendThread: message send to " + node.getDisplayName());

                    } catch (ExecutionException exception) {
                        sendmessage("SendThread: message failed to" + node.getDisplayName());
                        Log.e(TAG, "Send Task failed: " + exception);

                    } catch (InterruptedException exception) {
                        Log.e(TAG, "Send Interrupt occurred: " + exception);
                    }

                }

            } catch (ExecutionException exception) {
                sendmessage("Node Task failed: " + exception);
                Log.e(TAG, "Node Task failed: " + exception);

            } catch (InterruptedException exception) {
                Log.e(TAG, "Node Interrupt occurred: " + exception);
            }

        }
    }
}
