package edu.cs4730.wearnotidemo;

import android.os.Bundle;
import androidx.core.app.RemoteInput;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;

/**
 * This activity is just to receive the intent for the voice notification.
 * otherwise, it doesn't do anything.
 */

public class VoiceNotiActivity extends AppCompatActivity {
    String info;
    TextView logger;
    // Key for the string that's delivered in the action's intent
    private static final String EXTRA_VOICE_REPLY = "extra_voice_reply";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_noti);

        //note android developer page, shows this in a separate method, but not necessary.

        Bundle remoteInput = RemoteInput.getResultsFromIntent(getIntent());
        if (remoteInput != null) {
            info = remoteInput.getCharSequence(EXTRA_VOICE_REPLY).toString();
        } else {
            info = "No voice response.";
        }
        logger = findViewById(R.id.logger);
        logger.setText(info);
    }

}
