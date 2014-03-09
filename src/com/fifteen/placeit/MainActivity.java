package com.fifteen.placeit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fifteen.placeit.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;
import android.widget.Toast;

//Main driver, holds map and location client
public class MainActivity extends Activity implements OnMapClickListener, OnCameraChangeListener,
	OnMapLongClickListener, OnMarkerClickListener, ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

	private GoogleMap map;
	private List<Marker> mMarkers = new ArrayList<Marker>();

	private Map<String, Long> markerIdContainer;

	private double latitude;
	private double longitude;
	
	private float zoom;

	// Service definitions
	private MyService service;
	private ServiceManager serviceManager;

	// Debug tag
	private String tag = MainActivity.class.getSimpleName();

	private static LocationRequest locationRequest; 
	private static LocationClient locationClient; 
	
	// Preference variables
	private SharedPreferences preference;
	
	private static boolean loggedIn = false;

//BROADCAST HANDLER
	private BroadcastReceiver receiver = new BroadcastReceiver(){

		// Receives broadcast from service
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getParcelableExtra("bundle");
			long placeItId = bundle.getLong("id");
			LatLng placeItLocation = bundle.getParcelable("position");

			putMarkerOnMap(placeItId, placeItLocation);
		}
	};

//QUERY HANDLER
	private OnQueryTextListener queryListener = new OnQueryTextListener() { 
		@Override
		public boolean onQueryTextChange(String arg0) { return false; }

		@Override 
		public boolean onQueryTextSubmit(String address) { 
			locate(address);
			return true; 
		}
	}; 

	// Finds location of search bar query
	private void locate(String query) {
		Geocoder gc = new Geocoder(this);

		try {
			List<Address> list = gc.getFromLocationName(query, 1);
			Address address = list.get(0);
			goToLocation(address.getLatitude(), address.getLongitude());
			String locality = address.getLocality();
			Toast.makeText(this, locality, Toast.LENGTH_LONG).show();
		} catch( IOException e ) {
			Log.wtf(tag, "At locate()");
			Log.wtf(tag, e.toString());
			Toast.makeText(this, "Service down", Toast.LENGTH_LONG).show();
		}
	}

//ACTIVITY DEFINITIONS

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Setups map if it's not
		setUpMapIfNeeded();
		
		// Initializes map and listeners
		map.setMyLocationEnabled(true);
		map.setOnMapClickListener(this);
		map.setOnMapLongClickListener(this);
		map.setOnMarkerClickListener(this);
		map.setOnCameraChangeListener(this);
		
		// Get preference
		preference = getSharedPreferences(Constant.SP.SAVE, Context.MODE_PRIVATE);

		// Initialize marker ID container
		markerIdContainer = new HashMap<String, Long>();

		// Change MyLocation button position
		changeMyLocationButton();

		// Initialize camera
		initializeCamera();
		
		// Initialize location services and support
		initializeLocationService();

		// Initialize Service Manager
		serviceManager = new ServiceManager(this);
		
		// Starts service
		Intent intent = new Intent(this, MyService.class);
		startService(intent);
		
		showDialog();
	}


	// Initializes search bar and its support functions
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);

		// Initialize search bar
		MenuItem menuItem = menu.findItem(R.id.mainSearchBar);
		SearchView searchView = (SearchView) menuItem.getActionView();
		searchView.setQueryHint("Search");

		// Changes search bar icon
		int searchIconId = searchView.getContext().getResources().getIdentifier("android:id/search_button", null, null);
		ImageView searchIcon = (ImageView) searchView.findViewById(searchIconId);
		searchIcon.setImageResource(R.drawable.search);

		// Changes search bar UI
		int searchPlateId = searchView.getContext().getResources().getIdentifier("android:id/search_plate", null, null);
		View searchPlate = searchView.findViewById(searchPlateId);
		if (searchPlate != null) {
			searchPlate.setBackgroundColor(Color.DKGRAY);
			int searchTextId = searchPlate.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
			TextView searchText = (TextView) searchPlate.findViewById(searchTextId);
			if (searchText != null) {
				searchText.setTextColor(Color.WHITE);
				searchText.setHintTextColor(Color.WHITE);
			}
		}		

		searchView.setOnQueryTextListener(queryListener); 

		return super.onCreateOptionsMenu(menu);
	}

	// Handles selection inside action bar
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.mainCreateBtn:
			createPlaceIt();
			break;
		case R.id.mainListBtn:
			gotoListPage();
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	// Handles create a place it action bar button
	private void createPlaceIt() {
		LatLng position = new LatLng(0, 0);

		Bundle send = new Bundle();
		send.putParcelable("position", position);

		Intent i = new Intent(this, CreatePlaceItActivity.class);
		i.putExtra("bundle", send);
		startActivity(i);
	}

	// Binds service and fills map with place its
	public void onResume() {
		super.onResume();

		// Dynamically registers the broadcaster
		registerReceiver(receiver, new IntentFilter(MyService.NOTIFICATION));

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

	@Override 
	protected void onStart() { 
		super.onStart(); 
		locationClient.connect(); 
	}
	
	protected void onDestroy() {
		// Unbinds service and receiver
		service = serviceManager.unBindService();
		unregisterReceiver(receiver);
		
		// Saves zoom level
		preference.edit().putFloat(Constant.SP.ZOOM, zoom).commit();
		
		
		String latitudeString = String.valueOf(latitude);
		String longitudeString = String.valueOf(longitude);
		
		// Saves current location, prevents precision lost of putFloat()
		preference.edit().putString(Constant.SP.LAT, latitudeString).commit();
		preference.edit().putString(Constant.SP.LNG, longitudeString).commit();
		
		super.onDestroy();
	}

//ACTIVITY SUPPORT DEFINITIONS	

	// Change MyLocation button position
	private void changeMyLocationButton() {
		// Get MyLocation button position
		View locationButton = ( (View) findViewById(R.id.map).findViewById(1).getParent() ).findViewById(2);

		// Change MyLocation button position
		RelativeLayout.LayoutParams layout = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
		layout.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
		layout.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
		layout.setMargins(0, 0, 30, 30);
	}

	// Check if the map is instantiated
	private void setUpMapIfNeeded() {
		if (map == null) {
			map = ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
		}
	}
	
	// Initialize location services
	private void initializeLocationService() {
		locationRequest = LocationRequest.create(); 
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); 
		locationRequest.setInterval(Constant.L.NORMAL_INTERVAL); 
		locationRequest.setFastestInterval(Constant.L.FASTEST_INTERVAL);
		locationRequest.setSmallestDisplacement(Constant.L.UPDATE_DISTANCE);

		locationClient = new LocationClient(MainActivity.this, this, this);
	}
	
	// Initialize camera position
	private void initializeCamera() {
		// Get saved zoom
		zoom = preference.getFloat(Constant.SP.ZOOM, 18);
		
		// SharedPreferences has no getDouble(), prevents precision lost of getFloat()
		// Defaults at San Diego
		latitude = Double.parseDouble(preference.getString(Constant.SP.LAT, "32.7150"));
		longitude = Double.parseDouble(preference.getString(Constant.SP.LNG, "-117.1625"));
		
		goToLocation(latitude, longitude);
	}

//MARKER DEFINITIONS & SUPPORT
	// Fill the map with placeits
	// Only gets called when service is successfully bound
	private void fillMapWithPlaceIts() {
		ArrayList<AbstractPlaceIt> list = new ArrayList<AbstractPlaceIt>( service.getOnMapList() );

		// Refreshes the map with new data
		mMarkers.clear();
		markerIdContainer.clear();
		map.clear();

		Iterator<AbstractPlaceIt> it = list.iterator();
		while(it.hasNext()) {
			AbstractPlaceIt object = it.next();
			if(object.getCoordinate() == null)
				continue;
			putMarkerOnMap(object.getId(), object.getCoordinate());
		}
	}

	// Puts place it marker on map
	private void putMarkerOnMap(long placeItId, LatLng placeItLocation) {
		Marker addMarker = map.addMarker(new MarkerOptions()
		.position(placeItLocation)
		.icon(BitmapDescriptorFactory.fromResource(R.drawable.note)));
		mMarkers.add(addMarker);

		// Store placeit ID with marker ID in a hashmap for easy tracking
		markerIdContainer.put(addMarker.getId(), placeItId);
	}

//LOCATION HANDLERS
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) { 
		if (connectionResult.hasResolution()) { 
			try { 
				// Start an Activity that tries to resolve the error 
				connectionResult.startResolutionForResult( this, Constant.L.CONNECTION_FAILURE_RESOLUTION_REQUEST); 
			} catch (IntentSender.SendIntentException e) { 
				// Log the error 
				e.printStackTrace(); 
			} 
		} else { 
			Toast.makeText(this, "FAILURE!", Toast.LENGTH_LONG).show(); 
		} 
	} 

	@Override 
	public void onConnected(Bundle dataBundle) { 
		// TODO STOPPED THIS ZOMBIE PROCESS!!!
		//locationClient.requestLocationUpdates(locationRequest, this); 
	} 

	// Handles location changes
	@Override 
	public void onLocationChanged(Location location) { 
		// Saves current location
		latitude = location.getLatitude();
		longitude = location.getLongitude();
		
		// TODO Temporary location passing solution
		if( service != null){
			String latitudeString = String.valueOf(latitude);
			String longitudeString = String.valueOf(longitude);
			
			// Saves current location, prevents precision lost of putFloat()
			preference.edit().putString(Constant.SP.LAT, latitudeString).commit();
			preference.edit().putString(Constant.SP.LNG, longitudeString).commit();
		}
		
		goToLocation(latitude, longitude);
	} 

	@Override 
	public void onDisconnected() { 
		Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show(); 
	} 

//EVENT HANDLERS
	// List button handler
	// Sends user to list of place-it activity
	public void gotoListPage(View view) {
		// TODO Doing this
		if( loggedIn == false ) {
			new RequestPlacesAPI().update(latitude, longitude);
			loggedIn = true;
			return;
		}
		
		Log.wtf("PRINTING", new JSONParser().getAddress("political"));
	}
	
	public void gotoListPage() {
		Intent i = new Intent(this, PlaceItListActivity.class);
		startActivity(i);
	}
	
	@Override
	public void onCameraChange(CameraPosition camera) {
		zoom = camera.zoom;
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

	// Send user to detail page on marker click
	public boolean onMarkerClick(Marker marker) {
		Intent i = new Intent(this, PlaceItDetailActivity.class);
		i.putExtra("id", markerIdContainer.get(marker.getId()));
		startActivity(i);

		return false;
	}

	@Override
	public void onMapClick(LatLng position) {
		hideKeyboard();
		String mapClickMessage = "Hold tap to create Place It";
		Toast.makeText(this, mapClickMessage, Toast.LENGTH_SHORT).show();
	}
	
	void showDialog() {
	    DialogFragment newFragment = LoginFragment.newInstance();
	    //newFragment.setCancelable(false);
	    newFragment.show(getFragmentManager(), "dialog");
	}

	public void doPositiveClick() {
	    // Do stuff here.
	    Log.i("FragmentAlertDialog", "Positive click!");
	}

	public void doNegativeClick() {
	    // Do stuff here.
	    Log.i("FragmentAlertDialog", "Negative click!");
	}
	
	public void dialogCancel() {
		disconnect();
		finish();
	}
	
	private void disconnect() {
		Log.wtf(tag, "Dialog cancelled, shutting down EVERYTHING!");

		locationClient.removeLocationUpdates(this);
		locationClient.disconnect();
	}

	//EVENT HANDLERS SUPPORT	
	// Goes to location
	private void goToLocation(double latitude, double longitude) {
		updateCamera(new LatLng(latitude, longitude));
	}
	
	private void updateCamera(LatLng location) {
		Log.wtf(tag, "MOVE " + location.latitude + " & " + location.longitude + " Z " + zoom);
		
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, zoom));
	}

	// Hides the keyboard 
	private void hideKeyboard() {
		View view = findViewById(android.R.id.content);
		InputMethodManager input = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
		input.hideSoftInputFromWindow(view.getWindowToken(),0);
	}
}