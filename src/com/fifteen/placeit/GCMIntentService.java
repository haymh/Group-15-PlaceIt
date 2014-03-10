package com.fifteen.placeit;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;

public class GCMIntentService extends GCMBaseIntentService {
	

	public static final String FROM_GCM_SERVICE = "com.fifteen.placeit.gcm";
	
	public GCMIntentService(){
		super(Constant.GCM.SENDER_ID);
	}
	
	public GCMIntentService(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	protected void onError(Context arg0, String arg1) {
		
	}

	@Override
	protected void onMessage(Context arg0, Intent arg1) {
		Intent in = new Intent(FROM_GCM_SERVICE);
		sendBroadcast(in);
	}

	@Override
	protected void onRegistered(Context arg0, String arg1) {
		
	}

	@Override
	protected void onUnregistered(Context arg0, String arg1) {
		
	}
}
