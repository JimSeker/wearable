package edu.cs4730.wearnotidemo;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/*
 * This activity is simply here to receive the pentingintent
 * from the wearable device.  
 */


public class NotiActivity extends Activity {
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
		logger = (TextView) findViewById(R.id.logger);
		logger.setText(info);
		
	}

}
