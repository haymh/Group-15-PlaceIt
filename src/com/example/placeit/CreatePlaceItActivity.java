package com.example.placeit;

import com.google.android.gms.maps.model.LatLng;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class CreatePlaceItActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_place_it);
		
		// Get marker location
		Bundle bundle = getIntent().getParcelableExtra("bundle");
		LatLng location = bundle.getParcelable("position");
		
		TextView text = (TextView) findViewById(R.id.lat);
		text.setText(String.valueOf(location.latitude));
		
		text = (TextView) findViewById(R.id.lng);
		text.setText(String.valueOf(location.longitude));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.create_place_it, menu);
		return true;
	}
	
	public void onResume() {
		super.onResume();
	}
	
	public void sendMeBack(View view) {
		Intent i = new Intent();
		setResult(RESULT_OK, i);
		
		this.finish();
	}
	
	public void onDestroy(){
		
		super.onDestroy();
	}

}
