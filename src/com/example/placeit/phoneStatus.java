package com.example.placeit;

import android.app.Activity;
import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class phoneStatus extends Activity{
    Context b;
    
    public phoneStatus(MainActivity a){
        b = a;
    }

    public boolean checkGPS()
    {
        LocationManager mlocManager = 
                (LocationManager) b.getSystemService(Context.LOCATION_SERVICE);
           boolean enabled = mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
           return enabled;
                
    }
    public boolean checkWIFI()
    {
        ConnectivityManager connManager = (ConnectivityManager) b.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (mWifi.isConnected())
            return true;
        else
            return false;
    }
}