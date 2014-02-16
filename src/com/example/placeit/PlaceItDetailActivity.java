package com.example.placeit;

import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ListActivity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class PlaceItDetailActivity extends ListActivity {

	private MyService service; 
	private ServiceManager manager; 
	
	private PlaceIt placeIt;
	private long placeItId;
	
	private String tag = PlaceItDetailActivity.class.getSimpleName();
	
	private List<String> list = new ArrayList<String>();
	
//ACTIVITY DEFINITIONS
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_place_it_detail);
		
		// Gets id number of placeit passed by MainActivity
		placeItId = getIntent().getLongExtra("id", 0);
		
		// Initialize service manager, required for serving bind
		manager = new ServiceManager(this);
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
		
		list.add(placeIt.getTitle());
		
		String description = placeIt.getDescription();
		
		if( description.isEmpty() != true ) {
			Log.wtf(tag,  "Description <" + placeIt.getDescription() + ">");
			
			list.add(description);
		}
		
		list.add(String.valueOf(placeIt.getCreateDate()));
		
		list.add("Location: " + placeIt.getCoordinate().latitude + ", " + placeIt.getCoordinate().longitude);
		
		try {
			list.add(String.valueOf(placeIt.getPostDate()));
		} catch (Exception e) {
			Log.wtf(tag, e);
		}
		
		if( placeIt.isRepeatByMinute() == true ) {
			list.add(String.valueOf(placeIt.getRepeatedMinute()) + " minutes");
		}
		
		if( placeIt.isRepeatByWeek() == true) {
			boolean[] days = placeIt.getRepeatedDay().clone();
			
			for(int i = 0; i < 7; ++i) {
				if(days[i] == true) 
					list.add(getDayOfWeek(i));
			}
		}
		
		
		
		setListAdapter(new Adapter(PlaceItDetailActivity.this, R.layout.detail_list_object, list));
	}
	
	private String getDayOfWeek(int day) {
		switch(day) {
		case 0:
			return "Monday";
		case 1:
			return "Tuesday";
		case 2:
			return "Wednesday";
		case 3:
			return "Thursday";
		case 4:
			return "Friday";
		case 5:
			return "Saturday";
		case 6:
			return "Sunday";
		default:
			Log.wtf(tag, "Day " + String.valueOf(day) + " not found");
			return null;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.place_it_detail, menu);
		return true;
	}

	@Override
	protected void onDestroy() {
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
	
// UI DEFINTION
	public class Adapter extends ArrayAdapter<String> {
		
		public Adapter(Context context, int textResource, List<String> objects) {
			super(context, textResource, objects);
		}

		// Fills the list fragment
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;

			if(row == null) {
				LayoutInflater inflater = (LayoutInflater) PlaceItDetailActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				row = inflater.inflate(R.layout.detail_list_object, null);
			}

			String item = getItem(position);

			// Sets text into respective TextView
			if(item != null) {
				TextView text = (TextView) row.findViewById(R.id.detailListData);
				if(text != null)
					text.setText(item);
			}
			return row;
		}
	}
}
