package edu.cs4730.wearnotidemo;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.widget.TextView;

/**
 * This activity is simply here to receive the pentingintent
 * from the wearable device.
 */

public class NotiActivity extends AppCompatActivity {
    String info;
    TextView logger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noti);
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            info = "No information";
        } else {
            info = extras.getString("NotiID");
        }
        logger = findViewById(R.id.logger);
        logger.setText(info);
    }

}
