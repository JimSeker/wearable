package edu.cs4730.weardemo1;

import java.util.Random;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends Activity {
	private TextView mTextView;
	Random myRandom = new Random();
	ImageButton ib;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
		stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
			@Override
			public void onLayoutInflated(WatchViewStub stub) {
				mTextView = (TextView) stub.findViewById(R.id.text);
				mTextView.setText("   "+ myRandom.nextInt(10) + " ");
				ib = (ImageButton) stub.findViewById(R.id.myButton);
				ib.setOnClickListener(new OnClickListener() {
 
					@Override
					public void onClick(View v) {
						mTextView.setText("   "+ myRandom.nextInt(10) + " ");
						
					}
					
				});
				
			}
		});
	}

}
