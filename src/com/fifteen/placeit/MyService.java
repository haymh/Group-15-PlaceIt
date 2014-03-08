package com.fifteen.placeit;

import java.util.Date;

import com.fifteen.placeit.R;
import com.fifteen.placeit.PlaceIt.NumOfWeekRepeat;
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
	private final static long POST_TIME_LAG = 20000;
	private final IBinder mBinder = new LocalBinder();
	private DistanceManager dManager = new DistanceManager(this);
	private boolean stop = false;
	private DatabaseAccessor database;
	private Map<Long, AbstractPlaceIt> pulldown;
	private Map<Long, AbstractPlaceIt> onMap;
	private Map<Long, AbstractPlaceIt> prePost;
	private NotifyPostThread nThread;
	private int counter = 0; // id for notification

	public class LocalBinder extends Binder{
		public MyService getService(){
			return MyService.this;
		}
	}

	// a thread to check Place-it schedule and put them up, also check if there is any nearby place-it and notify user 
	private class NotifyPostThread extends Thread{
		public void run(){
			while(!stop){
				Iterator<AbstractPlaceIt> i = prePost.values().iterator();
				while(i.hasNext()){
					AbstractPlaceIt pi = i.next();
					try {
						if(pi.getSchedule().postNowOrNot()){
							i.remove();
							database.onMap(pi.getId());
							pi.status = AbstractPlaceIt.Status.ON_MAP;
							onMap.put(pi.getId(), pi);
							LatLng coordinate = pi.getCoordinate();
							if(coordinate == null)
								continue;
							Bundle send = new Bundle();
							send.putParcelable("position", coordinate);
							send.putLong("id", pi.getId());
							Intent in = new Intent(NOTIFICATION);
							in.putExtra("bundle", send);
							sendBroadcast(in);
						}
					} catch (ContradictoryScheduleException e) {
						e.printStackTrace();
					}
				}
				try {
					sleep(POST_TIME_LAG);
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
	public boolean createPlaceIt(String title, String description, int repeatedDayInWeek, int repeatedMinute, 
			WeeklySchedule.NumOfWeekRepeat numOfWeekRepeat, Date createDate, Date postDate, double latitude,
			double longitude, AbstractPlaceIt.Status status, String[] categories){
		AbstractPlaceIt pi = database.insertPlaceIt(title, description, repeatedDayInWeek, repeatedMinute,
				numOfWeekRepeat, createDate, postDate, latitude, longitude, status, categories);
		Log.v("myService createPlaceIt","pi is created");
		if(pi == null)
			return false;
		//active.put(pi.getId(), pi);
		prePost.put(pi.getId(),pi);
		return true;
	}
	
	public boolean createPlaceIt(String title, String description, int repeatedDayInWeek, int repeatedMinute, 
			WeeklySchedule.NumOfWeekRepeat numOfWeekRepeat, Date createDate, Date postDate, LatLng coordinate,
			AbstractPlaceIt.Status status, String[] categories){
		return createPlaceIt(title, description, repeatedDayInWeek, repeatedMinute, numOfWeekRepeat, createDate,
				postDate, coordinate.latitude, coordinate.longitude, status, categories);
	}


	// access this to get pull down list
	public Collection<AbstractPlaceIt> getPulldownList(){
		Log.v("MyService","getPulldownList");
		return pulldown.values();
	}

	// access this to get a list of PlaceIt object that is on map
	public Collection<AbstractPlaceIt> getOnMapList(){
		Log.v("MyService","getOnMapList");
		return onMap.values();
	}

	// access this to get a list of PlaceIt object that is going to be posted
	public Collection<AbstractPlaceIt> getPrepostList(){
		return prePost.values();
	}

	// access this to get a place-it by id
	public AbstractPlaceIt findPlaceIt(long id){
		AbstractPlaceIt pi = onMap.get(id);
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
			AbstractPlaceIt pi = onMap.get(id);
			if(pi == null){
				pi = prePost.remove(id);
			}else{
				onMap.remove(id);	
			}		
			pi.setStatus(AbstractPlaceIt.Status.PULL_DOWN);
			pulldown.put(id, pi);
		}
		return success;
	}

	// to discard a place-it from active or pulldown
	public boolean discardPlaceIt(long id){
		boolean success = database.discard(id);
		if(success){
			AbstractPlaceIt pi = onMap.get(id);
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
	public boolean repostPlaceIt(long id, LatLng currentLocation){
		AbstractPlaceIt pi = pulldown.get(id);
		if(pi.getCoordinate() != null){
			if(pi.trigger(currentLocation)){
				pi.schedule.extendPostDate(Calendar.MINUTE, 45);
				pi.schedule.setCreateDate(new Date());
			}
		}
		pi.setStatus(AbstractPlaceIt.Status.ACTIVE);
		database.repostPlaceIt(pi);
		pulldown.remove(id);
		prePost.put(id, pi);
		return true;
	}
	
	// call to make service check every place_it in onMap, try to fire place_it
	public void checkPlaceIts(LatLng currentLocation){
		Iterator<AbstractPlaceIt> onMapIterator = onMap.values().iterator();
		while(onMapIterator.hasNext()){
			AbstractPlaceIt pi = onMapIterator.next();
			if(pi.trigger(currentLocation)){
				if(pi.status == AbstractPlaceIt.Status.ACTIVE){
					onMapIterator.remove();
					database.active(pi.getId());
					prePost.put(pi.getId(), pi);
					notify(pi);
				}else if(pi.status == AbstractPlaceIt.Status.PULL_DOWN){
					onMapIterator.remove();
					database.pullDown(pi.getId());
					pulldown.put(pi.getId(), pi);
					notify(pi);
				}
			}			
		}
	}
	
	public void notify(AbstractPlaceIt pi){
		Intent intent = new Intent(MyService.this,PlaceItDetailActivity.class);
		intent.putExtra("id", pi.getId());
		PendingIntent resultPendingIntent = 
				PendingIntent.getActivity(MyService.this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
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
