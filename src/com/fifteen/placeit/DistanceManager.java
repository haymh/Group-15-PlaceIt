package com.fifteen.placeit;

import java.util.List;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

public class DistanceManager {
    private double currentLatitude;
    private double currentLongitude;
    private Location location;
    private Context context;
    
    private String tag = DistanceManager.class.getSimpleName();
    
    public DistanceManager(Context context) {
    	this.context = context;
    	location = null;
    }

    // Obtains user current location
    public void getCurrentLocation() {
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = manager.getProviders(true);

        for (int i = 0; i < providers.size(); i++) {
            location = manager.getLastKnownLocation(providers.get(i));
            if (location != null)
                break;
        }

        if (location != null) {
            currentLatitude = location.getLatitude();
            currentLongitude = location.getLongitude();
        }
    }
    
    // Pass back current coordinates
    public LatLng getCurrentCoordinates(){
    	getCurrentLocation();
    	return new LatLng(this.currentLatitude, this.currentLongitude);
    }
    
    // Calculates distance from user
    public float distanceTo(LatLng destination){
    	Location l = new Location("");
    	l.setLatitude(destination.latitude);
    	l.setLongitude(destination.longitude);
    	return location.distanceTo(l);
    }
    
    // Calculates distance from a point to another
    public float distanceTo(LatLng current, LatLng destination){
    	Location from = new Location("");
    	from.setLatitude(current.latitude);
    	from.setLongitude(current.longitude);
    	
    	Location to = new Location("");
    	to.setLatitude(destination.latitude);
    	to.setLongitude(destination.longitude);
    	
    	return from.distanceTo(to);
    }
}