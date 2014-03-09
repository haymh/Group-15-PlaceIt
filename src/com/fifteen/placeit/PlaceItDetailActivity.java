package com.fifteen.placeit;

import java.util.ArrayList;
import java.util.List;

import com.fifteen.placeit.R;

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
import android.widget.Button;
import android.widget.TextView;

// Displays the details of a place it
// Also handles details on notification
public class PlaceItDetailActivity extends ListActivity {

	private MyService service; 
	private ServiceManager manager; 
	
	private AbstractPlaceIt placeIt;
	private long placeItId;
	private int statusType;
	private boolean listIsFilled = false;
	
	private String tag = PlaceItDetailActivity.class.getSimpleName();
	
	private List<DetailContent> list;
	
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
				if( !listIsFilled ) {
					fillDetailPage();
					setUpOmniButton();
					listIsFilled = true;
				}
			}
		}.execute();
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
	
//UI SETTERS AND SUPPORT
	
	// Fills detail page, use DetailContent to format
	// Calls service to obtain Place It referenced by ID from MainActivity
	private void fillDetailPage() {
		placeIt = service.findPlaceIt(placeItId);
		list = new ArrayList<DetailContent>(new DetailContentFormatter(placeIt).getDetailsArray());
		
		setListAdapter(new Adapter(PlaceItDetailActivity.this, R.layout.detail_list_object, list));
	}
	

//BUTTON HANDLERS 
	
	// Handle omni button depending on place it type
	public void omniPlaceItHandler(View view) {	
		// Type 1 and 2: On Map Place It, To Be Posted Place It
		// Type 3: Pulled Down Place It
		switch(statusType) {
		case 1:
		case 2:
			pullDownHandler();
			break;
		case 3:
			service.repostPlaceIt(placeItId);
			this.finish();
			break;
		default:
			Log.wtf(tag, "Status " + statusType + " not supported");
			this.finish();
		}
	}
	
	// Handles periodic placeits pull down behavior
	public void pullDownHandler() {
		service.pulldownPlaceIt(placeItId);
		this.finish();
	}
	
	public void goBack(View view) {
		this.finish();
	}
	
	public void discardPlaceIt(View view) {
		service.discardPlaceIt(placeItId);
		this.finish();
	}
	
	// Set ups button for either Pull Down or Repost depending on Place It type
	public void setUpOmniButton() {
		Button omniButton = (Button) findViewById(R.id.detailOmniBtn);
		switch(statusType) {
		case 0:
		case 1:
		case 2:
			omniButton.setText("Pull Down");
			return;
		case 3:
			omniButton.setText("Repost");
			return;
		default:
			Log.wtf(tag, "Button of " + statusType + " could not be set");
		}
	}
	
//UI DEFINTION
	public class Adapter extends ArrayAdapter<DetailContent> {
		
		public Adapter(Context context, int textResource, List<DetailContent> objects) {
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

			DetailContent item = getItem(position);

			// Sets text into respective TextView
			if(item != null) {
				TextView description = (TextView) row.findViewById(R.id.detailListDescription);
				if(description != null) {
					description.setText(item.description);
					description.setTextSize(item.descriptionFontSize);
					description.setGravity(item.descriptionAlignment);
				}
				
				TextView content = (TextView) row.findViewById(R.id.detailListContent);
				if(content != null) {
					content.setText(item.content);
					content.setTextSize(item.contentFontSize);
					content.setGravity(item.contentAlignment);
				}
			}
			return row;
		}
	}
}
