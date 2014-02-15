package com.example.placeit;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class MainActivity extends Activity implements OnMapClickListener, CancelableCallback {

    private GoogleMap mMap;
    private List mMarkers = new ArrayList();
    private Iterator marker = mMarkers.iterator();
    
    // Location client, used to get location
    private LocationClient mLocationClient;
    
    private double latitude;
    private double longitude;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpMapIfNeeded();
        mMap.setMyLocationEnabled(true);
        mMap.setOnMapClickListener(this);
        
        // Sends user to current location
        gotoCurrentLocation();
        
        // Turns off compass
        mMap.getUiSettings().setCompassEnabled(false);
        
        /*
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
         }                                );
         */
         
          
         Button btnUpdate = (Button) findViewById(R.id.sendLocationBtn);
         btnUpdate.setOnClickListener(new View.OnClickListener() 
         { 
             @Override
             public void onClick(View v)
             {
                try {
                	if(checkWIFI())
                		geoLocate(v);
                	else
                	{
                		hideSoftKeyboard(v);
                		Toast.makeText(MainActivity.this,"WIFI is off, plz turn it on", Toast.LENGTH_LONG).show();
                	}
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    Log.wtf("Exception", e);
                    Toast.makeText(MainActivity.this,"Wrong input, try again :)", Toast.LENGTH_LONG).show();
                	}
                }
         });

         
    }
             
    // go to some coordinates         
     private void gotoLocation(double lat, double lng, float zoom)
     {
         LatLng ll = new LatLng(lat, lng);
         CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, zoom);
         mMap.moveCamera(update);
     }
     // address bar, take address
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
     public boolean checkGPS()
     {
    	 LocationManager mlocManager = 
    			 (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    	    boolean enabled = mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    	    return enabled;
    			 
     }
     public boolean checkWIFI()
     {
    	 ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    	 NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    	 if (mWifi.isConnected())
    	     return true;
    	 else
    		 return false;
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
    
    // List button handler
    // Sends user to list of place-it activity
    public void gotoListPage(View view)
    {
        Intent i = new Intent(this, PlaceItListActivity.class);
        startActivity(i);
        
        //Intent i = new Intent(this, TestActivity.class);
        //startActivity(i);
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
                 //.snippet("You can put other info here!"));
                 .snippet(""+(int)CalculationByDistance(new LatLng(latitude, longitude), pos)+"kms from current location"));
                 mMarkers.add(added);
             }
         });
 
         if(CalculationByDistance(new LatLng(latitude, longitude), pos) < 0.8){
        	 Intent intent = new Intent();
        	 PendingIntent pIntent = PendingIntent.getActivity(MainActivity.this,0,intent,0);
        	 Notification noti = new Notification.Builder(MainActivity.this)
        	 .setTicker("Ticker Title")
        	 .setContentTitle("Notification Content Title")
        	 .setContentText("Notification Content.")
        	 .setSmallIcon(R.drawable.ic_launcher)
        	 .setContentIntent(pIntent).getNotification();
        	 noti.flags=Notification.FLAG_AUTO_CANCEL;
        	 NotificationManager notificationManager = (NotificationManager)getSystemService(this.NOTIFICATION_SERVICE);
        	 notificationManager.notify(0,noti);
         }
         
         alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() 
         {
             public void onClick(DialogInterface dialog, int whichButton) 
             {
                 Toast.makeText(MainActivity.this, "Nothing added!", Toast.LENGTH_SHORT).show();
             }
         });
         alert.show();
     }
     public double CalculationByDistance(LatLng StartP, LatLng EndP) 
     {
	        int Radius=6371;//radius of earth in Km         
	        double lat1 = StartP.latitude;
	        double lat2 = EndP.latitude;
	        double lon1 = StartP.longitude;
	        double lon2 = EndP.longitude;
	        double dLat = Math.toRadians(lat2-lat1);
	        double dLon = Math.toRadians(lon2-lon1);
	        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
	        Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
	        Math.sin(dLon/2) * Math.sin(dLon/2);
	        double c = 2 * Math.asin(Math.sqrt(a));
	        double valueResult= Radius*c;
	        double km=valueResult/1;
	        DecimalFormat newFormat = new DecimalFormat("####");
	        int kmInDec =  Integer.valueOf(newFormat.format(km));
	        double meter=valueResult%1000;
	        int  meterInDec= Integer.valueOf(newFormat.format(meter));
	        Log.i("Radius Value",""+valueResult+"   KM  "+kmInDec+" Meter   "+meterInDec);
	
	        return Radius * c;
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