package com.fifteen.placeit;

import java.util.Collections;
import java.util.Date;
import java.util.Vector;

import com.fifteen.placeit.WeeklySchedule.NumOfWeekRepeat;
import com.google.android.gms.maps.model.LatLng;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class CreateCategoryPIActivity extends Activity{
	
	private Button cancel = null;
	private Button create = null;
	private Vector<String> vector=new Vector<String>();
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
	static int index = 0;
	
	private Date createDate = new Date();
	private Date postDate = new Date();
	private NumOfWeekRepeat numOfWeekRepea;
	// temp
	static private String[] catArray = new String[3];
	
	private ServiceManager serviceManager;
	private MyService service;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_category_pi);
	    
		vector.setSize(3);
		vector.clear();
		// listview adapter 		
		listview = (ListView)findViewById(R.id.cat_listview);
		//listview = getListView();
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, cat);
		listview.setAdapter(adapter);
		listview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		
		
		
		
		// create button
		create = (Button)findViewById(R.id.buttonCreate);
		create.setOnClickListener(new createListener());
		// cancel button
		cancel = (Button)findViewById(R.id.buttonCancel);
		cancel.setOnClickListener(new cancelListener());
		
		
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
	class createListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			SparseBooleanArray sba = listview.getCheckedItemPositions();
			int counter = 0;
			for(int i=0; i<sba.size();i++)
			{
				if(sba.valueAt(i))
					counter++;
			}
			if(counter>3 || counter==0)
			{
				if(counter>3){
					Toast.makeText(getApplicationContext(), "You've selected more than 3 categories.",
							Toast.LENGTH_SHORT).show();
				}
				else
				{
					Toast.makeText(getApplicationContext(), "You didn't select any category.",
							Toast.LENGTH_SHORT).show();
				}
			}
			else
			{
				for(int i=0; i<sba.size();i++)
				{
					if(sba.valueAt(i) && vector.size()<3)
					{
						//Toast.makeText(getApplicationContext(), cat[sba.keyAt(i)],Toast.LENGTH_SHORT).show();
						vector.add(cat[sba.keyAt(i)]);
					}
				}
				Collections.sort(vector);
				vector.copyInto(catArray);			

				create(v);
			}

		}	
		
	}

	// FIXME Working on this
	public void create(View view){

		boolean s = service.createPlaceIt(cat[index], "Testing Categories", 0, 0, 
				WeeklySchedule.NumOfWeekRepeat.ZERO, createDate, postDate, new LatLng(0,0), AbstractPlaceIt.Status.ON_MAP, catArray );

		if(s){
			Toast.makeText(this, "New Place-it Created", Toast.LENGTH_SHORT).show();
			this.finish();
		}else
			Toast.makeText(this, "New Place-it was not Created", Toast.LENGTH_SHORT).show();
		}
}

