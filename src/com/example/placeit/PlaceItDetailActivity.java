package com.example.placeit;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class PlaceItDetailActivity extends Activity {

	private MyService service; 
	private ServiceManager manager; 
	
	private PlaceIt placeIt;
	private long placeItId;
	
	private String tag = PlaceItDetailActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_place_it_detail);
		
		// Gets id number of placeit passed by MainActivity
		placeItId = getIntent().getLongExtra("id", 0);
		
		// Initialize service manager, required for serving bind
		manager = new ServiceManager(this);
		
		/*
		//TextView text1 = (TextView)  findViewById(R.id.editText1);
		//text1.setText(this.getDescription());
		//text1.setText("Description goes here");

		//TextView text2 = (TextView) findViewById(R.id.editText2);
		//text2.setText("Date Set for goes here");

		//DecimalFormat df = new DecimalFormat("#.##");
		//TextView text3 = (TextView) findViewById(R.id.editText3);
		//text3.setText(df.format(lat) + "," + df.format(lon));

		TextView text4 = (TextView) findViewById(R.id.editText4);
		text4.setText("Repetition Schedule goes here");
		*/
	}
	
	// Binds service
	// Calls fillDetailPage is successful
	public void onResume() {
		super.onResume();
		
		new AsyncTask<Void, Void, Integer>(){ 
			protected void onPreExecute() { }
			protected Integer doInBackground(Void... params) {
				while(service == null)
					service = manager.bindService();
				return Integer.valueOf(1);
			}
			
			protected void onPostExecute(Integer result) {
				fillDetailPage();
			}
		}.execute();
	}
	
	// Fills detail page
	// Calls service to obtain Place It referenced by ID from MainActivity
	private void fillDetailPage() {
		placeIt = service.findPlaceIt(placeItId);
		
		Log.wtf(tag, "PlaceIt ID " + placeItId + " : " + placeIt.getId());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.place_it_detail, menu);
		return true;
	}

	@Override
	public void onDestroy() {
		service = manager.unBindService();
		super.onDestroy();
	}
	
// BUTTON HANDLERS 
	public void discardPlaceIt(View view) {
		service.discardPlaceIt(placeItId);
	}
	
	public void pullDownPlaceIt(View view) {
		service.pulldownPlaceIt(placeItId);
	}
}
