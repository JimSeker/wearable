package edu.cs4730.wearabledatalayer2;

import android.os.Bundle;
import androidx.annotation.NonNull;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.List;
import java.util.concurrent.ExecutionException;


/*
 * Wear Device code  so I can kept this straight.
 *
 * This will receive messages (from a device/phone) via the datalayer (through the local listener code)
 * and display them to the wear device.  There is also a button to send a message
 * to the device/phone as well.
 *
 * if the wear device receives a message from the phone/device it will then send a message back
 * via the button on the wear device, it can also send a message to the device/phone as well.
 *    There is no auto response from the phone/device otherwise we would get caught in a loop!
 *
 * debugging over bluetooth.
 * https://developer.android.com/training/wearables/apps/debugging.html
 * adb forward tcp:4444 localabstract:/adb-hub
 * adb connect 127.0.0.1:4444
 */

public class MainActivity extends WearableActivity implements
    MessageClient.OnMessageReceivedListener {

    private final static String TAG = "Wear MainActivity";
    private TextView mTextView;
    Button myButton;
    int num = 1;
    String datapath = "/message_path";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = findViewById(R.id.text);
        //send a message from the wear.  This one will not have response.
        myButton = findViewById(R.id.wrbutton);
        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "Hello device " + num;
                //Requires a new thread to avoid blocking the UI
                new SendThread(datapath, message).start();
                num++;
            }
        });

        // Enables Always-on
        setAmbientEnabled();
    }

    @Override
    public void onResume() {
        super.onResume();
        Wearable.getMessageClient(this).addListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        Wearable.getMessageClient(this).removeListener(this);
    }

    /**
     *  The listener is in the code, in this example (instead in a separate listener).
     *  note, the listener is removed when the activity is paused.
     */
    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        Log.d(TAG, "onMessageReceived() A message from watch was received:"
            + messageEvent.getRequestId() + " " + messageEvent.getPath());
        if (messageEvent.getPath().equals("/message_path")) {  //don't think this if is necessary anymore.
            String message =new String(messageEvent.getData());
            Log.v(TAG, "Wear activity received message: " + message);
            // Display message in UI
            mTextView.setText(message);
            //here, send a message back.
            message = "Hello device " + num;
            //Requires a new thread to avoid blocking the UI
            new SendThread(datapath, message).start();
            num++;
        }
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
                        Log.v(TAG, "SendThread: message send to " + node.getDisplayName());

                    } catch (ExecutionException exception) {
                        Log.e(TAG, "Task failed: " + exception);

                    } catch (InterruptedException exception) {
                        Log.e(TAG, "Interrupt occurred: " + exception);
                    }

                }

            } catch (ExecutionException exception) {
                Log.e(TAG, "Task failed: " + exception);

            } catch (InterruptedException exception) {
                Log.e(TAG, "Interrupt occurred: " + exception);
            }
        }
    }
}
