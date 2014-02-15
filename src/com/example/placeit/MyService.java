package com.example.placeit;

import java.util.Date;

import com.example.placeit.PlaceIt.NumOfWeekRepeat;
import com.google.android.gms.maps.model.LatLng;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.*;

public class MyService extends Service {
	private final IBinder mBinder = new LocalBinder();
	private DatabaseAccessor database;
	private Map<Long, PlaceIt> active;
	private Map<Long, PlaceIt> pulldown;
	private Map<Long, PlaceIt> onMap;

	public class LocalBinder extends Binder{
		public MyService getService(){
			return MyService.this;
		}
	}
	
	private class NotifyThread extends Thread{
		public void run(){
			
		}
	}
	
	private class PostThread extends Thread{
		public void run(){
			
		}
	}
	
	public MyService() {
		
	}
	
	

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		database = new DatabaseAccessor(this);
		database.open();
		active = database.activePlaceIt();
		pulldown = database.pulldownPlaceIt();
		onMap = database.onMapPlaceIt();
	}



	@Override
	public void onRebind(Intent intent) {
		// TODO Auto-generated method stub
		super.onRebind(intent);
	}



	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("MyService", "Received start id " + startId + ": " + intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
	}



	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		return super.onUnbind(intent);
	}



	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		return mBinder;
	}

	
	//create a place-it, return a boolean value indicates if it's successful
	public boolean createPlaceIt(String title, String description, boolean repeatByMinute,
			int repeatedMinute, boolean repeatByWeek,
			int repeatedDayInWeek, NumOfWeekRepeat numOfWeekRepeat,
			Date createDate, Date postDate, LatLng coordinate){
		PlaceIt pi = database.insertPlaceIt(title, description, repeatByMinute, repeatedMinute, repeatByWeek,
				repeatedDayInWeek, numOfWeekRepeat, createDate, postDate, coordinate.latitude, coordinate.longitude);
		Log.v("myService createPlaceIt","pi is created");
		if(pi == null)
			return false;
		active.put(pi.getId(), pi);
		return true;
	}
	
	// access this to get active list
	public Collection<PlaceIt> getActiveList(){
		Log.v("MyService","getActiveList");
		return active.values();
	}
	
	// access this to get pull down list
	public Collection<PlaceIt> getPulldownList(){
		Log.v("MyService","getPulldownList");
		return pulldown.values();
	}
	
	// access this to get a list of PlaceIt object that is on map
	public Collection<PlaceIt> getOnMapList(){
		Log.v("MyService","getOnMapList");
		return onMap.values();
	}
	
	// access this to get a place-it by id
	public PlaceIt findPlaceIt(long id){
		PlaceIt pi = active.get(id);
		if(pi == null)
			pi = pulldown.get(id);
		return pi;
	}
	
	// to pull down a place from active
	public boolean pulldownPlaceIt(long id){
		boolean success = database.pullDown(id);
		if(success)
			pulldown.put(id,active.remove(id));
		return success;
	}
	
	// to discard a place-it from active or pulldown
	public boolean discardPlaceIt(long id){
		boolean success = database.discard(id);
		if(success){
			if(active.get(id) != null)
				active.remove(id);
			else
				pulldown.remove(id);
		}
		return success;
	}
	
	public boolean repostPlaceIt(long id){
		boolean success = database.onMap(id);
		if(success){
			active.put(id, database.findPlaceIt(id));
		}
		return success;
	}
}
