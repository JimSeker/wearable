package edu.cs4730.wearabledatalayer3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

/**
 * this is the main activity on the device/phone  (just to kept everything straight)
 * <p>
 * The device will setup a local listener to receive data from the wear device and display them to the screen.
 * <p>
 * It also setups up a button, so it can send a message to the wear device, the wear device will auto
 * response to the message.   This code does not auto response, otherwise we would get caught in a loop.
 * <p>
 * This is all done via the datalayer dataClient that requires om.google.android.gms:play-services-wearable
 * in the gradle (both wear and mobile).  Also the applicationId MUST be the same in both files as well
 * both use a the "/message_path" to send/receive messages.
 * <p>
 * Note, since both the mobile and wear are looking at the same path, they will all get the data sent,
 * since it appears to be broadcast format, not individual.
 * <P>
 *  To send something more then a simple string see https://github.com/googlesamples/android-DataLayer,
 *  which sends a picture via the asset pieces.
 * <p>
 * debugging over bluetooth.
 * https://developer.android.com/training/wearables/apps/debugging.html
 * adb forward tcp:4444 localabstract:/adb-hub
 * adb connect 127.0.0.1:4444
 */


public class MainActivity extends AppCompatActivity implements
    DataClient.OnDataChangedListener,
    View.OnClickListener {


    String datapath = "/data_path";
    Button mybutton;
    TextView logger;
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
    }

    // add data listener
    @Override
    public void onResume() {
        super.onResume();
        Wearable.getDataClient(this).addListener(this);
    }

    //remove data listener
    @Override
    public void onPause() {
        super.onPause();
        Wearable.getDataClient(this).removeListener(this);
    }

    /**
     * simple method to add the log TextView.
     */
    public void logthis(String newinfo) {
        if (newinfo.compareTo("") != 0) {
            logger.append("\n" + newinfo);
        }
    }

    //button listener
    @Override
    public void onClick(View v) {
        String message = "Hello wearable " + num;
        //Requires a new thread to avoid blocking the UI
        sendData(message);
        num++;
    }

    /**
     * Receives the data, note since we are using the same data base, we will also receive
     * data that we sent as well.  Likely should add some kind of id number to datamap, so it
     * can tell between mobile device and wear device.  or this maybe the functionality you want.
     */
    @Override
    public void onDataChanged(@NonNull DataEventBuffer dataEventBuffer) {
        Log.d(TAG, "onDataChanged: " + dataEventBuffer);
        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                String path = event.getDataItem().getUri().getPath();
                if (datapath.equals(path)) {
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    String message = dataMapItem.getDataMap().getString("message");
                    Log.v(TAG, "Wear activity received message: " + message);
                    // Display message in UI
                    logthis(message);
                } else {
                    Log.e(TAG, "Unrecognized path: " + path);
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                Log.v(TAG, "Data deleted : " + event.getDataItem().toString());
            } else {
                Log.e(TAG, "Unknown data event Type = " + event.getType());
            }
        }
    }


    /**
     * Sends the data.  Since it specify a client, everyone who is listening to the path, will
     * get the data.
     */
    private void sendData(String message) {
        PutDataMapRequest dataMap = PutDataMapRequest.create(datapath);
        dataMap.getDataMap().putString("message", message);
        PutDataRequest request = dataMap.asPutDataRequest();
        request.setUrgent();

        Task<DataItem> dataItemTask = Wearable.getDataClient(this).putDataItem(request);
        dataItemTask
            .addOnSuccessListener(new OnSuccessListener<DataItem>() {
                @Override
                public void onSuccess(DataItem dataItem) {
                    Log.d(TAG, "Sending message was successful: " + dataItem);
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Sending message failed: " + e);
                }
            })
        ;
    }
}
