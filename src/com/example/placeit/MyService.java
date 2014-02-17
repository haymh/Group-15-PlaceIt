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
import android.media.RingtoneManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.*;

public class MyService extends Service {
	public static final String NOTIFICATION = "com.example.placeit.service.receiver";
	private final static double RANGE = 0.8;
	private final static long NOTIFY_TIME_LAG = 1000;
	private final static long POST_TIME_LAG = 20;
	private final IBinder mBinder = new LocalBinder();
	private DistanceManager dManager = new DistanceManager(this);
	private boolean stop = false;
	private DatabaseAccessor database;
	private Map<Long, PlaceIt> pulldown;
	private Map<Long, PlaceIt> onMap;
	private Map<Long, PlaceIt> prePost;
	private NotifyPostThread nThread;

	public class LocalBinder extends Binder{
		public MyService getService(){
			return MyService.this;
		}
	}

	// a thread to check Place-it schedule and put them up, also check if there is any nearby place-it and notify user 
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
						if(!pi.isRepeated()){
							pi.setStatus(PlaceIt.Status.PULL_DOWN);
							pulldown.put(pi.getId(), pi);
						}else{
							pi.setStatus(PlaceIt.Status.ACTIVE);
							prePost.put(pi.getId(), pi);
						}
						Intent intent = new Intent(MyService.this,PlaceItDetailActivity.class);
						intent.putExtra("id", pi.getId());
						PendingIntent resultPendingIntent = 
								PendingIntent.getActivity(MyService.this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
						
						//build notification
						NotificationCompat.Builder mBuilder =
								new NotificationCompat.Builder(MyService.this)
								.setSmallIcon(R.drawable.note)
								.setContentTitle(pi.getTitle())
								.setContentText(pi.getDescription())
								.setContentIntent(resultPendingIntent)
								.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
								.setAutoCancel(true);
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
							
							//intent to broadcast receiver in MainActivity to notify putting up a place-it icon
							Intent in = new Intent(NOTIFICATION);
							Bundle send = new Bundle();
							send.putParcelable("position", pi.getCoordinate());
							send.putLong("id", pi.getId());
							in.putExtra("bundle", send);
							sendBroadcast(in);
						}
					}
				}

				try {
					sleep(NOTIFY_TIME_LAG);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
		}
	}

	public MyService() {}

	@Override
	public void onCreate() {
		super.onCreate();
		database = new DatabaseAccessor(this);
		database.open();
		// get list of place-it from Data base
		pulldown = database.pulldownPlaceIt();
		onMap = database.onMapPlaceIt();
		prePost = database.prepostPlaceIt();
		// launch the thread
		(nThread = new NotifyPostThread()).start(); 
	}

	@Override
	public void onRebind(Intent intent) {
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
		stop = true; // set stop to true make thread stop running
		super.onDestroy();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}

	@Override
	public IBinder onBind(Intent intent) {
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
			if(pi == null){
				pi = prePost.get(id);
				if(!pi.isRepeated()){
					prePost.remove(id);
					pi.setStatus(PlaceIt.Status.PULL_DOWN);
					pulldown.put(id, pi);
				}
			}else{
				onMap.remove(id);
				if(!pi.isRepeated()){
					pi.setStatus(PlaceIt.Status.PULL_DOWN);
					pulldown.put(id, pi);
				}else{
					pi.setStatus(PlaceIt.Status.ACTIVE);
					prePost.put(id, pi);
				}
			}		
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

	// to repost a place-it from pulldown list
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
