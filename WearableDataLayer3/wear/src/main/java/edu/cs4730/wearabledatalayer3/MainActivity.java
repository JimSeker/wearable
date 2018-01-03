package edu.cs4730.wearabledatalayer3;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.Date;

public class MainActivity extends WearableActivity implements
    DataClient.OnDataChangedListener {

    private final static String TAG = "Wear MainActivity";
    private TextView mTextView;
    Button myButton;
    int num = 1;
    String datapath = "/data_path";

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
                sendData(message);
                num++;
            }
        });

        // Enables Always-on
        setAmbientEnabled();
    }

    @Override
    public void onResume() {
        super.onResume();
        Wearable.getDataClient(this).addListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        Wearable.getDataClient(this).removeListener(this);
    }

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
                    mTextView.setText(message);
                    //here, send a message back.
                  //  message = "Hello device " + num;
                    //Requires a new thread to avoid blocking the UI
                  //  sendData(message);
                  //  num++;
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
     *  Sends the data.
     */
    private void sendData(String message) {
        PutDataMapRequest dataMap = PutDataMapRequest.create(datapath);
        dataMap.getDataMap().putString("message", message);
        PutDataRequest request = dataMap.asPutDataRequest();
        request.setUrgent();

        Task<DataItem> dataItemTask = Wearable.getDataClient(this).putDataItem(request);

        dataItemTask.addOnSuccessListener(new OnSuccessListener<DataItem>() {
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
