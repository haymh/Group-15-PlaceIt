package com.fifteen.placeit;

import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class PhoneStatus {
	private Context context;

	public PhoneStatus(Context context){
		this.context = context;
	}

	public boolean checkGPS()
	{
		LocationManager mlocManager = 
				(LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		boolean enabled = mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		return enabled;

	}
	public boolean checkWIFI()
	{
		ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (mWifi.isConnected())
			return true;
		else
			return false;
	}
}