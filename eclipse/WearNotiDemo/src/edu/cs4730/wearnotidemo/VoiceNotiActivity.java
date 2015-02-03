package edu.cs4730.wearnotidemo;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.RemoteInput;
import android.widget.TextView;

/*
 * This activity is just to receive the intent for the voice notification.
 * otherwise, it doesn't do anything.
 */

public class VoiceNotiActivity extends Activity {
	String info;
	TextView logger;
	// Key for the string that's delivered in the action's intent
	private static final String EXTRA_VOICE_REPLY = "extra_voice_reply";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_voice_noti);
		
		//note android developer page, shows this in a separate method, but not necessary.
		
		Bundle remoteInput = RemoteInput.getResultsFromIntent( getIntent());
	    if (remoteInput != null) {
	        info = remoteInput.getCharSequence(EXTRA_VOICE_REPLY).toString();
	    } else {
			info = "No voice reponse.";
		} 
		logger = (TextView) findViewById(R.id.logger);
		logger.setText(info);
	}

}
