package com.example.placeit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity implements OnMapClickListener, OnInfoWindowClickListener, CancelableCallback,
	OnMapLongClickListener, OnMarkerClickListener {

	private GoogleMap mMap;
	private List<Marker> mMarkers = new ArrayList<Marker>();
	private Iterator marker = mMarkers.iterator();
	
	private Map<String, Long> markerIdContainer;
	
	//private HashMap<String, long> marker;

	private double latitude;
	private double longitude;
	
	private DistanceManager distanceManager;
	private PhoneStatus phoneStatus;
	
	// Service definitions
	private MyService service;
	private ServiceManager serviceManager;
	
	// Return activity request codes
	private final int CREATE_PLACEIT = 1;
	
	private String tag = MainActivity.class.getSimpleName();

//ACTIVITY DEFINITIONS
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Setups map if it's not
		setUpMapIfNeeded();
		
		// Initializes map
		mMap.setMyLocationEnabled(true);
		mMap.setOnMapClickListener(this);
		mMap.setOnMapLongClickListener(this);
		mMap.setOnInfoWindowClickListener(this);
		mMap.setOnMarkerClickListener(this);
		
		// Initialize support classes
		distanceManager = new DistanceManager(this);
		phoneStatus = new PhoneStatus(this); 
		
		// Sends user to current location
		gotoCurrentLocation();

		// Turns off compass
		mMap.getUiSettings().setCompassEnabled(false);
		
		// Initialize Service Manager
		serviceManager = new ServiceManager(this);
		
		// Initialize marker ID container
		markerIdContainer = new HashMap<String, Long>();
		Intent intent = new Intent(this, MyService.class);
		startService(intent);
	}
	
	// Check if the map is instantiated
	private void setUpMapIfNeeded() {
		if (mMap == null) {
			mMap = ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	// Binds service
	// Fills map with placeits
	public void onResume() {
		super.onResume();
		
        new AsyncTask<Void, Void, Integer>() {
	        protected void onPreExecute() {}

	        protected Integer doInBackground(Void... params) {
	            while(service == null)
	            	service = serviceManager.bindService();
	            return Integer.valueOf(1);
	        }
	        
	        protected void onPostExecute(Integer result) {
	        	fillMapWithPlaceIts();
	        }
	    }.execute();
	}
	
	// Fill the map with placeits
	// Only gets called when service is successfully bound
	private void fillMapWithPlaceIts() {
		ArrayList<PlaceIt> list = new ArrayList<PlaceIt>( service.getOnMapList() );
		//Log.wtf(tag, "Filling map");
		
		// Refreshes the map with new data
		mMarkers.clear();
		markerIdContainer.clear();
		mMap.clear();
		
		Iterator<PlaceIt> it = list.iterator();
		while(it.hasNext()) {
			PlaceIt object = it.next();
			
			Marker addMarker = mMap.addMarker(new MarkerOptions()
			.position(object.getCoordinate())
			.icon(BitmapDescriptorFactory.fromResource(R.drawable.note)));
			mMarkers.add(addMarker);
			
			//Log.wtf(tag, String.valueOf(object.getId()) + " vs " + addMarker.getId());
			
			// Store placeit ID with marker ID for tracking
			markerIdContainer.put(addMarker.getId(), object.getId());
			
			//Log.wtf(tag, "ID Container size: " + markerIdContainer.size());
		}
	}
	
	@Override
	public void onCancel() {}

	@Override
	public void onFinish() {
		if (marker.hasNext()) {
			Marker current = (Marker) marker.next();
			mMap.animateCamera(CameraUpdateFactory.newLatLng(current.getPosition()), 2000, this);
			current.showInfoWindow();
		}
	}
	
	protected void onDestroy() {
		service = serviceManager.unBindService();
		
		super.onDestroy();
	}

//EVENT HANDLERS
	
	// List button handler
	// Sends user to list of place-it activity
	public void gotoListPage(View view) {
		Intent i = new Intent(this, PlaceItListActivity.class);
		startActivity(i);
	}
	
	// Goto button handler 
	public void sendToLocation(View view) {
		try {
			if(phoneStatus.checkWIFI())
				geoLocate(view);
			else
			{
				hideSoftKeyboard(view);
				Toast.makeText(MainActivity.this,"WIFI is off, plz turn it on", Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			Log.wtf("Exception", e);
			Toast.makeText(MainActivity.this,"Wrong input, try again :)", Toast.LENGTH_LONG).show();
		}
	}
	
	// Send user to create a place it activity on a long tap (Hold)
	@Override
	public void onMapLongClick(LatLng position) {
		// Send location to activity
		Bundle send = new Bundle();
		send.putParcelable("position", position);
		
		Intent i = new Intent(this, CreatePlaceItActivity.class);
		i.putExtra("bundle", send);
		startActivity(i);
	}
	
	public boolean onMarkerClick(Marker marker) {
		Intent i = new Intent(this, PlaceItDetailActivity.class);
		i.putExtra("id", markerIdContainer.get(marker.getId()));
		startActivity(i);
		
		return false;
	}

	@Override
	public void onMapClick(LatLng position) {
		final LatLng pos = position;

		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("New Place-It");
		alert.setMessage("Please enter a Place-It Title:");
		// Set an EditText view to get user input
		final EditText input = new EditText(this);
		alert.setView(input);
		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = input.getText().toString();
				Toast.makeText(MainActivity.this, "Tag added!", Toast.LENGTH_SHORT).show();
				Marker added = mMap.addMarker(new MarkerOptions()
				.position(pos)
				.title(value)
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.note))
				.snippet(""+(int)distanceManager.calculateDistance(new LatLng(latitude, longitude)) +"kms from current location"));
				mMarkers.add(added);
			}
		});

		if(distanceManager.calculateDistance(new LatLng(latitude, longitude)) < 0.8) {
			Intent intent = new Intent();
			PendingIntent pIntent = PendingIntent.getActivity(MainActivity.this,0,intent,0);
			Notification noti = new Notification.Builder(MainActivity.this)
			.setTicker("Place-It Title")
			.setContentTitle("Notification Content Title")
			.setContentText("Notification Content.")
			.setSmallIcon(R.drawable.note)
			.setContentIntent(pIntent).getNotification();
			noti.flags=Notification.FLAG_AUTO_CANCEL;
			NotificationManager notificationManager = (NotificationManager)getSystemService(this.NOTIFICATION_SERVICE);
			notificationManager.notify(0,noti);
		}

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				Toast.makeText(MainActivity.this, "Nothing added!", Toast.LENGTH_SHORT).show();
			}
		});
		alert.show();
	}

	@Override
	public void onInfoWindowClick(Marker added) {
		Intent myIntent = new Intent(MainActivity.this, PlaceItDetailActivity.class);
		myIntent.putExtra("ID", added.getId());
		myIntent.putExtra("Description", added.getSnippet());
		myIntent.putExtra("Name", added.getTitle());
		myIntent.putExtra("Latitude", latitude);
		myIntent.putExtra("Longitude", longitude);

		MainActivity.this.startActivity(myIntent);
		//Toast.makeText(MainActivity.this, "This needs to open the Details page, but I dunno how to do it. :(", Toast.LENGTH_SHORT).show();
		//PlaceItDetailActivity.setVisible(View.VISIBLE);              
	}
	
//EVENT HANDLERS SUPPORT
	
	// Address bar, takes an address or coordinate
	public void geoLocate(View v) throws IOException
	{
		hideSoftKeyboard(v);
		EditText editText = (EditText) findViewById(R.id.sendLocation);
		String location = editText.getText().toString();

		if(checkIfIsCoordante(location))
		{
			String geoData = editText.getText().toString();
			String[] coordinate = geoData.split(",");
			latitude = Double.valueOf(coordinate[0]).doubleValue();
			longitude = Double.valueOf(coordinate[1]).doubleValue();
			mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude), 12),2000,null);
		}
		else
		{
			Geocoder gc = new Geocoder(this);
			List<Address> list = gc.getFromLocationName(location, 1);
			Address add = list.get(0);
			gotoLocation(add.getLatitude(), add.getLongitude(), 15);

			String locality = add.getLocality();
			Toast.makeText(this, locality, Toast.LENGTH_LONG).show();
		}
	}
	
	// Go to some location  
	private void gotoLocation(double lat, double lng, float zoom) {
		LatLng ll = new LatLng(lat, lng);
		CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, zoom);
		mMap.moveCamera(update);
	}
	
	//check if the input is a coordinate
	public boolean checkIfIsCoordante(String location)
	{
		String string = location;
		string.replaceAll("\\s+","");
		int i=0;
		int j=0;
		int k=0;
		String str[] = new String[3];
		for(;i<string.length();i++)
		{
			if(string.charAt(i)==',')
			{
				str[k] = string.substring(j, i-1);
				str[++k] = string.substring(i,1+i++);
				str[++k] = string.substring(i);
				break;
			}
			return false;
		}       
		if(str[0].matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+") &&
				str[2].matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+"))
			return true;
		else
			return false;
	}
	private void hideSoftKeyboard(View v)
	{
		InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(v.getWindowToken(),0);
	}

	// This sends the camera to the user current location
	// Called during onCreate()
	private void gotoCurrentLocation()
	{
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		List<String> providers = lm.getProviders(true);

		Location l = null;
		for (int i = 0; i < providers.size(); i++) {
			l = lm.getLastKnownLocation(providers.get(i));
			if (l != null)
				break;
		}

		if (l != null) {
			latitude = l.getLatitude();
			longitude = l.getLongitude();
		}

		mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude), 12),2000,null);
	}
	
//TEST BUTTON HANDLERS, intent whatever you want
	public void test1(View view){
		Intent myIntent = new Intent(MainActivity.this, PlaceItDetailActivity.class);
		MainActivity.this.startActivity(myIntent);
		
	}
	
	public void test2(View view){
	}
	
	public void test3(View view){
		Random r = new Random();
		
		double lat = (latitude - 0.5) + r.nextDouble();
		double lon = (longitude - 0.5) + r.nextDouble();
		
		LatLng local = new LatLng(lat, lon);
		
		service.createPlaceIt("Test title length to test how long", "Test description length to test how long description can be", 
				r.nextInt(40) < 20, r.nextInt(),
				r.nextInt(40) < 20, PlaceIt.FRI + PlaceIt.MON, PlaceIt.NumOfWeekRepeat.genNumOfWeekRepeat(r.nextInt(3) + 1),
				new Date(), new Date(), local);
	}
}