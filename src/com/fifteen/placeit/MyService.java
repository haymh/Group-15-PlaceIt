package com.fifteen.placeit;

import java.util.Date;

import com.fifteen.placeit.R;
import com.fifteen.placeit.WeeklySchedule.NumOfWeekRepeat;
import com.google.android.gms.maps.model.LatLng;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.*;

public class MyService extends Service {
	public static final String NOTIFICATION = "com.example.placeit.service.receiver";
	
	private BroadcastReceiver receiver = new BroadcastReceiver(){

		// Receives broadcast from intent service
		@Override
		public void onReceive(Context context, Intent intent) {

			//TODO: get data from database
			Log.wtf("Service", "I got something from gcm");
		}
	};
	
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

	// TODO ZOO Location variables
	private SharedPreferences preference;
	private RequestPlacesAPI requestPlacesAPI;
	private LatLng location;
	private double latitude;
	private double longitude;

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
							if(preference.getBoolean(Constant.SP.U.LOGIN, false)){
								if(ServerUtil.changeStatus(pi.id, AbstractPlaceIt.Status.ON_MAP) != ServerUtil.OK)
									continue ;
								preference.edit().putLong(Constant.SP.TIME, new Date().getTime()).commit();
							}
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

		// XXX Get universal data
		preference = getSharedPreferences(Constant.SP.SAVE, Context.MODE_PRIVATE);

		// XXX Request Places API data
		requestPlacesAPI = new RequestPlacesAPI(fetchCurrentLocation());
		// TODO: change it to intentService
		registerReceiver(receiver, new IntentFilter(GCMIntentService.FROM_GCM_SERVICE));
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
		if(preference.getBoolean(Constant.SP.U.LOGIN, false)){
			if(ServerUtil.createPlaceIt(pi.getPlaceItInfoMap()) != ServerUtil.OK){
				database.discard(pi.getId());
				return false;
			}
			// update last update time to now
			// <<<< add code here
			preference.edit().putLong(Constant.SP.TIME, new Date().getTime()).commit();
		}

		//active.put(pi.getId(), pi);
		onMap.put(pi.getId(),pi);
		LatLng coordinate = pi.getCoordinate();
		if(coordinate != null){
			Bundle send = new Bundle();
			send.putParcelable("position", coordinate);
			send.putLong("id", pi.getId());
			Intent in = new Intent(NOTIFICATION);
			in.putExtra("bundle", send);
			sendBroadcast(in);
		}
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
		if(preference.getBoolean(Constant.SP.U.LOGIN, false)){
			if(ServerUtil.changeStatus(id, AbstractPlaceIt.Status.PULL_DOWN) != ServerUtil.OK)
				return false;
			// update last update time to now
			// <<<< add code here
			preference.edit().putLong(Constant.SP.TIME, new Date().getTime()).commit();
		}

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
		if(preference.getBoolean(Constant.SP.U.LOGIN, false)){
			if(ServerUtil.deletePlaceIt(id) != ServerUtil.OK)
				return false;
			// update last update time to now
			// <<<< add code here
			preference.edit().putLong(Constant.SP.TIME, new Date().getTime()).commit();
		}

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
	// TODO ZOO Changed this
	//public boolean repostPlaceIt(long id, LatLng currentLocation){
	public boolean repostPlaceIt(long id){
		if(preference.getBoolean(Constant.SP.U.LOGIN, false)){
			if(ServerUtil.changeStatus(id, AbstractPlaceIt.Status.ACTIVE) != ServerUtil.OK)
				return false;
			// update last update time to now new Date().getTime()
			// <<<<< add code here
			preference.edit().putLong(Constant.SP.TIME, new Date().getTime()).commit();
		}

		AbstractPlaceIt pi = pulldown.get(id);
		if(pi.getCoordinate() != null){

			// TODO ZOO Pass current location
			LatLng currentLocation = fetchCurrentLocation();
			if(pi.getCoordinate() != null){
				if(pi.trigger(currentLocation)){
					pi.schedule.extendPostDate(Calendar.MINUTE, 45);
					pi.schedule.setCreateDate(new Date());
				}
			}
		}
		pi.setStatus(AbstractPlaceIt.Status.ACTIVE);
		// XXX Added address refresh on for category repost
		pi.removeKeyFromInfoMap(Constant.PI.ADDRESS);
		database.repostPlaceIt(pi);
		pulldown.remove(id);
		prePost.put(id, pi);
		return true;
	}

	// TODO ZOO Get current location
	private LatLng fetchCurrentLocation() {
		// Gotten as string to prevent precision lost
		double latitude = Double.parseDouble(preference.getString(Constant.SP.LAT, "32.7150"));
		double longitude = Double.parseDouble(preference.getString(Constant.SP.LNG, "-117.1625"));

		return new LatLng(latitude, longitude);
	}

	// call to make service check every place_it in onMap, try to fire place_it
	public void checkPlaceIts(LatLng currentLocation){
		// TODO Grabs updates from Places API
		requestPlacesAPI.update(currentLocation);

		Iterator<AbstractPlaceIt> onMapIterator = onMap.values().iterator();
		while(onMapIterator.hasNext()){
			AbstractPlaceIt pi = onMapIterator.next();
			if(pi.trigger(currentLocation)){
				if(pi.status == AbstractPlaceIt.Status.ACTIVE){
					if(preference.getBoolean(Constant.SP.U.LOGIN, false)){
						if(ServerUtil.changeStatus(pi.id, AbstractPlaceIt.Status.ACTIVE) != ServerUtil.OK) {
							continue;
						}
					}
					// update last update time to now
					// <<< add code here
					preference.edit().putLong(Constant.SP.TIME, new Date().getTime()).commit();

					onMapIterator.remove();
					database.active(pi.getId());
					prePost.put(pi.getId(), pi);
					notify(pi);
				}else if(pi.status == AbstractPlaceIt.Status.PULL_DOWN){
					if(preference.getBoolean(Constant.SP.U.LOGIN, false)){
						if(ServerUtil.changeStatus(pi.id, AbstractPlaceIt.Status.ACTIVE) != ServerUtil.OK) {
							continue;
						}
					}
					// update last update time to now
					// <<<<add code here
					preference.edit().putLong(Constant.SP.TIME, new Date().getTime()).commit();

					onMapIterator.remove();
					database.pullDown(pi.getId());
					pulldown.put(pi.getId(), pi);
					notify(pi);
				}
				// category and location placeit have different notification
				// when category gets triggered, pull down all category place it has the same category
				// so, pull down by category in database, database accessor return me list of pulled down categories ids
			}			
		}
	}

	private void notify(AbstractPlaceIt pi){
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

	// login this account with server, return status code 400---fail 200---success 404---not found
	public int login(String username, String password, String regId){
		return ServerUtil.loginWithMultipleAttempt(username, password, regId);
	}

	// register this account with server,return status code 400---fail 200---success 404---not found
	public int register(String username, String password, String regId){
		return ServerUtil.registerWithMultipleAttempt(username, password, regId);
	}

	// Update location
	public void updateLocation(LatLng location) {
		this.location = location;
	}

	public void updateLocation(double latitude, double longitude) {
		location = new LatLng(latitude, longitude);
	}
}
