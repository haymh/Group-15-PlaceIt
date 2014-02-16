package com.example.placeit;

import java.util.Date;

import com.example.placeit.PlaceIt.NumOfWeekRepeat;
import com.google.android.gms.maps.model.LatLng;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.*;

public class MyService extends Service {
	private final static double RANGE = 0.8;
	private final static long NOTIFY_TIME_LAG = 5000;
	private final static long POST_TIME_LAG = 4;
	private final IBinder mBinder = new LocalBinder();
	private DistanceManager dManager = new DistanceManager(this);
	private boolean stop = false;
	private DatabaseAccessor database;
	//private Map<Long, PlaceIt> active;
	private Map<Long, PlaceIt> pulldown;
	private Map<Long, PlaceIt> onMap;
	private Map<Long, PlaceIt> prePost;
	private NotifyPostThread nThread;

	public class LocalBinder extends Binder{
		public MyService getService(){
			return MyService.this;
		}
	}

	private class NotifyPostThread extends Thread{
		private int i = 0;
		private int counter = 0;

		public void run(){
			while(!stop){
				Iterator<PlaceIt> onMapIterator = onMap.values().iterator();			
				dManager.getCurrentLocation();
				while(onMapIterator.hasNext()){
					PlaceIt pi = onMapIterator.next();
					if(dManager.distanceTo(pi.getCoordinate()) <= 800){
						database.pullDown(pi.getId());
						onMapIterator.remove();
						pi.setStatus(PlaceIt.Status.PULL_DOWN);
						pulldown.put(pi.getId(), pi);
						Intent intent = new Intent(MyService.this,PlaceItDetailActivity.class);
						intent.putExtra("id", pi.getId());
						PendingIntent resultPendingIntent = 
								PendingIntent.getActivity(MyService.this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
						
						NotificationCompat.Builder mBuilder =
								new NotificationCompat.Builder(MyService.this)
								.setSmallIcon(R.drawable.note)
								.setContentTitle(pi.getTitle())
								.setContentText(pi.getDescription())
								.setContentIntent(resultPendingIntent);
						TaskStackBuilder stackBuilder = TaskStackBuilder.create(MyService.this);
						stackBuilder.addParentStack(PlaceItDetailActivity.class);
						stackBuilder.addNextIntent(intent);
						NotificationManager mNotificationManager =
								(NotificationManager) MyService.this.getSystemService(Context.NOTIFICATION_SERVICE);
						mNotificationManager.notify(counter++, mBuilder.build());
					}
				}

				i++;
				if(i >= POST_TIME_LAG){
					i = 0;
					Iterator<PlaceIt> i = prePost.values().iterator();
					while(i.hasNext()){
						PlaceIt pi = i.next();
						if(pi.getPostDate().before(new Date())){
							i.remove();
							database.onMap(pi.getId());
							pi.setStatus(PlaceIt.Status.ON_MAP);
							onMap.put(pi.getId(), pi);
							onMapIterator = onMap.values().iterator();	

						}
					}
				}

				try {
					sleep(NOTIFY_TIME_LAG);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
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
		//active = database.activePlaceIt();
		pulldown = database.pulldownPlaceIt();
		onMap = database.onMapPlaceIt();
		prePost = database.prepostPlaceIt();
		(nThread = new NotifyPostThread()).start();
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
	public void onDestroy() {
		// TODO Auto-generated method stub
		stop = true;
		super.onDestroy();
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
		//active.put(pi.getId(), pi);
		prePost.put(pi.getId(),pi);
		return true;
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

	// access this to get a list of PlaceIt object that is going to be posted
	public Collection<PlaceIt> getPrepostList(){
		return prePost.values();
	}

	// access this to get a place-it by id
	public PlaceIt findPlaceIt(long id){
		PlaceIt pi = onMap.get(id);
		if(pi == null){
			pi = pulldown.get(id);
			if(pi == null)
				pi = prePost.get(id);
		}
		return pi;
	}

	// to pull down a place from active
	public boolean pulldownPlaceIt(long id){
		boolean success = database.pullDown(id);
		if(success){
			PlaceIt pi = onMap.get(id);
			if(pi == null)
				pi = prePost.remove(id);
			else
				onMap.remove(id);
			pi.setStatus(PlaceIt.Status.PULL_DOWN);
			pulldown.put(id,pi);
		}
		return success;
	}

	// to discard a place-it from active or pulldown
	public boolean discardPlaceIt(long id){
		boolean success = database.discard(id);
		if(success){
			PlaceIt pi = onMap.get(id);
			if(pi == null){
				pi = prePost.get(id);
				if(pi == null)
					pulldown.remove(id);
				else
					prePost.remove(id);
			}else
				onMap.remove(id);
		}
		return success;
	}

	public boolean repostPlaceIt(long id){
		PlaceIt pi = pulldown.get(id).clone();
		pi.extendPostDate(Calendar.MINUTE, 45);
		pi.setCreateDate(new Date());
		pi.setStatus(PlaceIt.Status.ACTIVE);
		boolean success = database.repostPlaceIt(pi);
		if(success){		
			pulldown.remove(id);
			prePost.put(id, pi);
		}
		return success;
	}
}
