package com.example.placeit;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ListActivity;
import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class PlaceItDetailActivity extends ListActivity {

	private MyService service; 
	private ServiceManager manager; 
	
	private PlaceIt placeIt;
	private long placeItId;
	private int statusType;
	
	private String tag = PlaceItDetailActivity.class.getSimpleName();
	
	private List<DetailContent> list = new ArrayList<DetailContent>();
	
	private final int SMALLFONT = 16;
	private final int MEDIUMFONT = 20;
	private final int TITLEFONT = 40;
	
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
				setUpOmniButton();
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
		
		String status = "";
		statusType = placeIt.getStatus().getValue();
		switch(statusType) {
		case 1: 
			status = "ON MAP";
			break;
		case 2:
			status = "TO BE POSTED";
			break;
		case 3:
			status = "PULLED DOWN";
			break;
		}
		list.add(new DetailContent(status, 10, Gravity.RIGHT));
		
		list.add(new DetailContent("TITLE", placeIt.getTitle(), TITLEFONT));
		
		String description = placeIt.getDescription();
		if( !description.isEmpty() ) 
			list.add(new DetailContent("DESCRIPTION", description, MEDIUMFONT));
				
		Date dateCreated = placeIt.getCreateDate();
		Date dateToBePosted = placeIt.getPostDate();
		
		list.add(new DetailContent("DATE", dateParser(dateCreated), SMALLFONT));
		
		DecimalFormat decimal = new DecimalFormat("#.####");
		list.add(new DetailContent("LOCATION", "(" + decimal.format(placeIt.getCoordinate().latitude)
				+ ", " + decimal.format(placeIt.getCoordinate().longitude) + ")", SMALLFONT));
		
		if( !dateToBePosted.equals(dateCreated) )
			list.add(new DetailContent("DATE to be POSTED", dateParser(dateToBePosted), SMALLFONT));
		
		if( placeIt.isRepeatByMinute() == true )
			list.add(new DetailContent("MINUTES to be POSTED", String.valueOf(placeIt.getRepeatedMinute()) + " minutes", SMALLFONT));
		
		if( placeIt.isRepeatByWeek() == true )
			weekParser();
		else 
			Log.wtf(tag, "Repeat by week, not true");
		
		setListAdapter(new Adapter(PlaceItDetailActivity.this, R.layout.detail_list_object, list));
	}
	
	// Parses date string
	private String dateParser(Date date) {
		DateFormat day = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
		DateFormat hour = new SimpleDateFormat("hh:mm aa", Locale.US);
		
		String parsedDate = day.format(date);
		String parsedHour = hour.format(date);
		
		return "<B>" + parsedDate + "</B> " + parsedHour;
	}
	
	// Parses weekly information
	private void weekParser() {
		int weeklyRepeat = placeIt.getNumOfWeekRepeat().getValue();
		String week = "";
		if(weeklyRepeat > 1)
			week = "REPEAT EVERY " + weeklyRepeat + " WEEKS";
		else
			week = "REPEAT WEEKLY";
		
		boolean[] days = placeIt.getRepeatedDay();
		String daysPosted = "";
		int dayNumbers[] = {6, 0, 1, 2, 3, 4, 5};
		
		for( int i : dayNumbers ) {
			if(days[i] == true) 
				daysPosted += "<B>" + getDayOfWeek(i) + "</B> ";
			else
				daysPosted += "<font color=#D8D8D8 >" + getDayOfWeek(i) + "</font> ";		
		}
		
		if( !daysPosted.isEmpty() )
			list.add(new DetailContent(week, daysPosted, MEDIUMFONT));
	}
	
	private String getDayOfWeek(int day) {
		switch(day) {
		case 0:
			return "M";
		case 1:
			return "T";
		case 2:
			return "W";
		case 3:
			return "T";
		case 4:
			return "F";
		case 5:
			return "S";
		case 6:
			return "S";
		default:
			Log.wtf(tag, "Day " + String.valueOf(day) + " not found");
			return "";
		}
	}

//BUTTON HANDLERS 
	
	// Handle omni button depending on place it type
	public void omniPlaceItHandler(View view) {
		
		// Type 1 and 2: On Map Place It, To Be Posted Place It
		// Type 3: Pulled Down Place It
		switch(statusType) {
		case 1:
		case 2:
			service.pulldownPlaceIt(placeItId);
			this.finish();
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
		if( placeIt.isRepeated() ) {
			Toast toast = Toast.makeText(this, "Repeated Place-It's stays active on pull down", Toast.LENGTH_SHORT);
			toast.show();
		}
			
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
				if(description != null)
					description.setText(item.description);
				
				TextView content = (TextView) row.findViewById(R.id.detailListContent);
				if(content != null) {
					if(item.contentFontSize > 0)
						content.setTextSize(item.contentFontSize);
					if(item.contentAlignment > 0)
						content.setGravity(item.contentAlignment);
					
					Spanned stringHTML = Html.fromHtml(item.content);
					content.setText(stringHTML);
				}
			}
			return row;
		}
	}
}
