package com.example.placeit;

import java.util.Iterator;
import java.util.List;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

@SuppressLint("NewApi")
public class MainActivity extends Activity implements OnMapClickListener, CancelableCallback {
	GoogleMap mMap;
	private List<Marker> mMarkers;
	private Iterator marker;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpMapIfNeeded();
        mMap.setMyLocationEnabled(true);
        mMap.setOnMapClickListener(this);
        Button btnReTrack = (Button) findViewById(R.id.retrack);
        btnReTrack.setOnClickListener(new View.OnClickListener() { 
        @Override
        public void onClick(View v) {
        marker = mMarkers.iterator();
        if (marker.hasNext()) {
        Marker current = (Marker) marker.next();
        mMap.animateCamera(CameraUpdateFactory.newLatLng(current.getPosition()), 2000, MainActivity.this);
        current.showInfoWindow();
        }
        }
        });
    }
    private void setUpMapIfNeeded() {
    	 // Do a null check to confirm that we have not already instantiated the map.
    	 if (mMap == null) {
    	 mMap = ((MapFragment)getFragmentManager().findFragmentById(R.id.map))
    	 .getMap();
    	 // Check if we were successful in obtaining the map.
    	 if (mMap != null) {
    	 // The Map is verified. It is now safe to manipulate the map.
    	 }
    	 }
    	
    	}
    @Override
    public void onMapClick(LatLng position) {
    	final LatLng pos = position;
    	 
    	 AlertDialog.Builder alert = new AlertDialog.Builder(this);
    	 alert.setTitle("New Marker");
    	 alert.setMessage("Please enter a Marker Title:");
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
    	 .snippet("You can put other info here!"));
    	 
    	 mMarkers.add(added);
    	 }
    	 });
    	 alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
    	 public void onClick(DialogInterface dialog, int whichButton) {
    	 Toast.makeText(MainActivity.this, "Nothing added!", Toast.LENGTH_SHORT).show();
    	 }
    	 });
    	 alert.show();
    }
	public void onCancel() {
		// TODO Auto-generated method stub
		
	}
	public void onFinish() {
		// TODO Auto-generated method stub
		
	}
}
