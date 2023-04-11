package edu.cs4730.wearsensorsdemo;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.wear.ambient.AmbientModeSupport;
import androidx.wear.widget.BoxInsetLayout;

/**
 * This is a example of how to access the sensors.  it just displays them to the screen.
 *
 * verified to work on pixel watch.
 */
public class MainActivity extends AppCompatActivity implements AmbientModeSupport.AmbientCallbackProvider {


    BoxInsetLayout mContainerView;
    TextView mTextView, mData;


    private SensorManager sensorManager;

    private List<Sensor> sensors;
    private Sensor sensor;
    private SensorEventListener listener;

    String TAG = "WearActivity";


    // Declare an ambient mode controller, which will be used by the activity to determine if the current mode is ambient.
    private AmbientModeSupport.AmbientController ambientController;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ambientController = AmbientModeSupport.attach(this);


        mContainerView = findViewById(R.id.container);
        mTextView = findViewById(R.id.text);
        mData = findViewById(R.id.data);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        //write out to the log all the sensors the device has.
        sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        if (sensors.size() < 1) {
            Toast.makeText(this, "No sensors returned from getSensorList", Toast.LENGTH_SHORT).show();
            Log.wtf(TAG, "No sensors returned from getSensorList");
        }
        Sensor[] sensorArray = sensors.toArray(new Sensor[sensors.size()]);
        for (int i = 0; i < sensorArray.length; i++) {
            Log.wtf(TAG, "Found sensor " + i + " " + sensorArray[i].toString());
        }
    }


    //part of the template code.  modified to add my textview.
    private void updateDisplay() {
        if (ambientController.isAmbient()) {
            mContainerView.setBackgroundColor(Color.BLACK);
            mTextView.setTextColor(Color.WHITE);
            mData.setTextColor(Color.WHITE);

        } else {
            mContainerView.setBackground(null);
            mTextView.setTextColor(Color.BLACK);
            mData.setTextColor(Color.BLACK);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerSensor();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterSensor();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //just to make sure.
        unregisterSensor();
    }

    void registerSensor() {
        //just in case
        if (sensorManager == null)
            sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        sensors = sensorManager.getSensorList(Sensor.TYPE_GAME_ROTATION_VECTOR);
        if (sensors.size() > 0)
            sensor = sensors.get(0);

        listener = new SensorEventListener() {
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // I have no desire to deal with the accuracy events

            }

            @Override
            public void onSensorChanged(SensorEvent event) {
                //just set the values to a textview so they can be displayed.
                if (event.sensor.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR) {
                    String msg = " x: " + String.valueOf(event.values[0]) +
                        "\n y: " + String.valueOf(event.values[1]) +
                        "\n z: " + String.valueOf(event.values[2]); //+
                    //"\n 3: " + String.valueOf(event.values[3]) +    //for the TYPE_ROTATION_VECTOR these 2 exist.
                    //"\n 4: " + String.valueOf(event.values[4]);
                    mData.setText(msg);
                }
            }
        };
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME);

    }

    void unregisterSensor() {
        if (sensorManager != null && listener != null) {
            sensorManager.unregisterListener(listener);
        }
        //clean up and release memory.
        sensorManager = null;
        listener = null;
    }

    private class MyAmbientCallback extends AmbientModeSupport.AmbientCallback {
        @Override
        public void onEnterAmbient(Bundle ambientDetails) {
            // Handle entering ambient mode
            super.onEnterAmbient(ambientDetails);
            updateDisplay();

        }

        @Override
        public void onExitAmbient() {
            // Handle exiting ambient mode
            updateDisplay();
            super.onExitAmbient();
        }

        @Override
        public void onUpdateAmbient() {
            // Update the content
            super.onUpdateAmbient();
            updateDisplay();
        }
    }

    @Override
    public AmbientModeSupport.AmbientCallback getAmbientCallback() {
        return new MyAmbientCallback();

    }
}
