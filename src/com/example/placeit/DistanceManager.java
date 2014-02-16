package com.example.placeit;

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
    	
    	getCurrentLocation();
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
    
    /*
    // Calculate distance from user
    public double calculateDistance(LatLng fromHere) {
    	getCurrentLocation();
    	
    	// Radius of the Earth in km
        double radius = 6371;     
        
        double lat1 = currentLatitude;
        double lat2 = fromHere.latitude;
        
        double lon1 = currentLongitude;
        double lon2 = fromHere.longitude;
        
        double dLat = Math.toRadians(lat2-lat1);
        double dLon = Math.toRadians(lon2-lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
        Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult= radius*c;
        double km=valueResult/1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec =  Integer.valueOf(newFormat.format(km));
        double meter=valueResult%1000;
        int  meterInDec= Integer.valueOf(newFormat.format(meter));
        
        Log.i("Radius Value",""+valueResult+"   KM  "+kmInDec+" Meter   "+meterInDec);

        return radius * c;
    }
    */
}