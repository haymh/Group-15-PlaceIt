package com.example.placeit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class MainActivity extends Activity implements OnMapClickListener, CancelableCallback {

	private GoogleMap mMap;
	private List mMarkers = new ArrayList();
	private Iterator marker = mMarkers.iterator();
	

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setUpMapIfNeeded();
		mMap.setMyLocationEnabled(true);
		mMap.setOnMapClickListener(this);
		
		 Button btnReTrack = (Button) findViewById(R.id.retrack);
		 btnReTrack.setOnClickListener(new View.OnClickListener() 
		 { 
			 @Override
			 public void onClick(View v) 
			 {
				 marker = mMarkers.iterator();
				 if (marker.hasNext()) 
				 {
				 Marker current = (Marker) marker.next();
				 mMap.animateCamera(CameraUpdateFactory.newLatLng(current.getPosition()), 2000, MainActivity.this);
				 current.showInfoWindow();
				 }
			 }
		 }								);
		 
		 Button btnUpdate = (Button) findViewById(R.id.sendLocationBtn);
		 btnUpdate.setOnClickListener(new View.OnClickListener() 
		 { 
			 @Override
			 public void onClick(View v) 
			 {
				 EditText editText = (EditText) findViewById(R.id.sendLocation);
				 String geoData = editText.getText().toString();
				 String[] coordinate = geoData.split(",");
				 double latitude = Double.valueOf(coordinate[0]).doubleValue();
				 double longitude = Double.valueOf(coordinate[1]).doubleValue();
				 mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude), 12),2000,null);
			 }
		 }								);

		 
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private void setUpMapIfNeeded() 
	{
		 // Do a null check to confirm that we have not already instantiated the map.
		 if (mMap == null) 
		 {
			 mMap = ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
			 // Check if we were successful in obtaining the map.
			 if (mMap != null) 
			 {
			 // The Map is verified. It is now safe to manipulate the map.
			 }
		 }
	}
	
	@Override
	 public void onMapClick(LatLng position) 
	 {
	 
		 final LatLng pos = position;
		 
		 AlertDialog.Builder alert = new AlertDialog.Builder(this);
		 alert.setTitle("New Marker");
		 alert.setMessage("Please enter a Marker Title:");
		 // Set an EditText view to get user input 
		 final EditText input = new EditText(this);
		 alert.setView(input);
		 alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() 
		 {
			 public void onClick(DialogInterface dialog, int whichButton) 
			 {
				 String value = input.getText().toString();
				 Toast.makeText(MainActivity.this, "Tag added!", Toast.LENGTH_SHORT).show();
				 Marker added = mMap.addMarker(new MarkerOptions()
				 .position(pos)
				 .title(value)
				 .snippet("You can put other info here!"));
			 
				 mMarkers.add(added);
			 }
		 });
		 
		 alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() 
		 {
			 public void onClick(DialogInterface dialog, int whichButton) 
			 {
				 Toast.makeText(MainActivity.this, "Nothing added!", Toast.LENGTH_SHORT).show();
			 }
		 });
		 alert.show();
	 }

	@Override
	public void onCancel() {
		// TODO Auto-generated method stub
		
	}

	@Override
	 public void onFinish() 
	 {
		 if (marker.hasNext()) 
		 {
			 Marker current = (Marker) marker.next();
			 mMap.animateCamera(CameraUpdateFactory.newLatLng(current.getPosition()), 2000, this);
			 current.showInfoWindow();
		 }
	 }
}