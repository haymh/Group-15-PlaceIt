package com.fifteen.placeit;

import java.util.Date;

import com.fifteen.placeit.WeeklySchedule.NumOfWeekRepeat;
import com.google.android.gms.maps.model.LatLng;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class CreateCategoryPIActivity extends Activity implements AdapterView.OnItemClickListener {
	
	private Button cancel = null;
	private static String cat[] = {"accounting", "airport", "amusement_park","aquarium","art_gallery","atm","bakery",
									"bank","bar","beauty_salon","bicycle_store","book_store","bowling_alley","bus_station",
									"cafe","campground","car_dealer","car_rental","car_repair","car_wash","casino","cemetery",
									"church","city_hall","clothing_store","convenience_store","courthouse","dentist","department_store",
									"doctor","electrician","electronics_store","embassy","establishment","finance","fire_station",
									"florist","food","funeral_home","furniture_store","gas_station","general_contractor",
		                            "grocery_or_supermarket","gym","hair_care","hardware_store","health","hindu_temple","home_goods_store",
		                            "hospital","insurance_agency","jewelry_store","laundry","lawyer","library","liquor_store",
		                            "local_government_office","locksmith","lodging","meal_delivery","meal_takeaway","mosque",
		                            "movie_rental","movie_theater","moving_company","museum","night_club","painter","park",
		                            "parking","pet_store","pharmacy","physiotherapist","place_of_worship","plumber","police",
		                            "post_office","real_estate_agency","restaurant","roofing_contractor","rv_park","school",
		                            "shoe_store","shopping_mall","spa","stadium","storage","store","subway_station","synagogue",
		                            "taxi_stand","train_station","travel_agency","university","veterinary_care","zoo"};
	private ListView listview = null;
	private CheckBox checkbox = null;
	static int index = 0;
	
	private Date createDate = new Date();
	private Date postDate = new Date();
	private NumOfWeekRepeat numOfWeekRepea;
	// temp
	static private String[] tempArray = new String[1];
	
	private ServiceManager serviceManager;
	private MyService service;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_category_pi);
	    
		// listview adapter 
		
		listview = (ListView)findViewById(R.id.cat_listview);
		//listview = getListView();
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.cat_single_row, R.id.catView, cat);
		listview.setAdapter(adapter);
		
		//listview.setItemsCanFocus(false);
		//this.getListView().setSelector(R.drawable.selector);
		
		listview.setOnItemClickListener(this);
		
		
		
		// cancel button
		cancel = (Button)findViewById(R.id.buttonCancel);
		cancel.setOnClickListener(new cancelListener());
		
		// check box
		//checkbox = (CheckBox)findViewById(R.id.checkbox);
		
		// Instantiate service manager
		serviceManager = new ServiceManager(this);
	}
	
	// Resume & bind service
	public void onResume() {
		super.onResume();
		// ensure service is not null, also not block the UI, so launch a AsyncTask
		new AsyncTask<Void, Void, Integer>() {
	        protected void onPreExecute() {}

	        protected Integer doInBackground(Void... params) {
	            while(service == null)
	            	service = serviceManager.bindService();
	            return Integer.valueOf(1);
	        }
	        
	        protected void onPostExecute(Integer result) { }
	    }.execute();
	}
	
	// Unbind service
	public void onDestroy(){
		service = serviceManager.unBindService();
		super.onDestroy();
	}
	
	class cancelListener implements OnClickListener{
		public void onClick(View v) {			
			finish();
		}		
	}

	// FIXME Working on this
	public void create(View view){
		// only for testing
		tempArray[0] = cat[index];
		/*
		boolean s = service.createPlaceIt(cat[index], null, 0, 0, null, createDate,
				postDate, 0, 0, AbstractPlaceIt.Status.ON_MAP, tempArray);
		*/

		
		//		null, createDate, postDate, new LatLng(0, 0), AbstractPlaceIt.Status.ON_MAP, tempArray );
		// TODO REPLACE THIS TOP
		/* 
		public boolean createPlaceIt(String title, String description, int repeatedDayInWeek, int repeatedMinute, 
				WeeklySchedule.NumOfWeekRepeat numOfWeekRepeat, Date createDate, Date postDate, LatLng coordinate,
				AbstractPlaceIt.Status status, String[] categories){
		*/
		final ProgressDialog dialog = ProgressDialog.show(this,
				"Posting Data...", "Please wait...", false);
		
		new AsyncTask<Void, Void, Boolean>(){

			@Override
			protected Boolean doInBackground(Void... params) {
				// TODO Auto-generated method stub
				return service.createPlaceIt(cat[index], "Testing Categories", 0, 0, 
						WeeklySchedule.NumOfWeekRepeat.ZERO, createDate, postDate, new LatLng(0,0), AbstractPlaceIt.Status.ON_MAP, tempArray );
			}
			
			@Override
			protected void onPostExecute(Boolean params) {
				dialog.dismiss();
				if(params){
					Toast.makeText(CreateCategoryPIActivity.this, "New Category Place-it Created", Toast.LENGTH_SHORT).show();
					CreateCategoryPIActivity.this.finish();
				}else
					Toast.makeText(CreateCategoryPIActivity.this, "New Category Place-it was not Created", Toast.LENGTH_SHORT).show();
			}
			
		}.execute();
		dialog.show();
	}
		
	/*
	public void onclick(View view) {
		
		Log.wtf("Something", "It works");
		//CheckBox c = (CheckBox) findViewById(R.id.cat_listview);
		myFancyMethod(view);
		
	}
	
	private void myFancyMethod(View view) {
		ListView temp = (ListView) findViewById(R.id.cat_listview);
		Toast.makeText(this, temp.getCount(), Toast.LENGTH_SHORT).show();
	}*/

	/*
	public boolean validate()
	{
		int count=0;
		
	}


	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int i,
			long l) {
		Log.wtf("Something", "It works");
		//Checkable child = (Checkable) parent.getChildAt(i);
		//child.toggle();
		CheckBox temp = (CheckBox) view;
		Toast.makeText(this, temp.getText()+" is selected "+ i, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		
	}*/

	@Override
	public void onItemClick(AdapterView<?> l, View v, int i, long arg3) {

		//TextView temp = (TextView) v;
		Toast.makeText(this, "number "+ i+" is selected which is "+cat[i], Toast.LENGTH_SHORT).show();
		index = i;
		this.create(v);
		finish();
	}
}
