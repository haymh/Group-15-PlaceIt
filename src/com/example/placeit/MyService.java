package com.example.placeit;

import java.util.Date;

import com.example.placeit.PlaceIt.NumOfWeekRepeat;
import com.google.android.gms.maps.model.LatLng;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.*;

public class MyService extends Service {
	private final static double RANGE = 0.8;
	private final static long NOTIFY_TIME_LAG = 5000;
	private final static long POST_TIME_LAG = 3600000;
	private final IBinder mBinder = new LocalBinder();
	private DistanceManager dManager = new DistanceManager(this);
	private boolean stop = false;
	private DatabaseAccessor database;
	private Map<Long, PlaceIt> active;
	private Map<Long, PlaceIt> pulldown;
	private Map<Long, PlaceIt> onMap;
	private Map<Long, PlaceIt> prePost;
	private Iterator<PlaceIt> onMapIterator;

	private NotifyThread nThread;
	private PostThread pThread;

	public class LocalBinder extends Binder{
		public MyService getService(){
			return MyService.this;
		}
	}

	private class NotifyThread extends Thread{
		private long timeLag;
		public NotifyThread(long timeLag){
			this.timeLag = timeLag;
		}
		public void run(){
			while(!stop){
				synchronized(onMap){
					onMapIterator = onMap.values().iterator();
				}
				dManager.getCurrentLocation();
				while(onMapIterator.hasNext()){
					PlaceIt pi = null;
					pi = onMapIterator.next();
					if(dManager.calculateDistance(pi.getCoordinate()) <= 0.8){
						Intent intent = new Intent(MyService.this,PlaceItDetailActivity.class);
						intent.putExtra("id", pi.getId());
						PendingIntent pIntent = PendingIntent.getActivity(MyService.this,0,new Intent(MyService.this,PlaceItDetailActivity.class),0);
						Notification noti = new Notification.Builder(MyService.this)
						.setTicker("Place-It Title")
						.setContentTitle(pi.getTitle())
						.setContentText(pi.getDescription())
						.setSmallIcon(R.drawable.note)
						.setContentIntent(pIntent).getNotification();
						noti.flags=Notification.FLAG_AUTO_CANCEL;
						NotificationManager notificationManager = (NotificationManager)getSystemService(Activity.NOTIFICATION_SERVICE);
						notificationManager.notify(0,noti);
					}
				}
				try {
					sleep(timeLag);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private class PostThread extends Thread{
		private long timeLag;
		public PostThread(long timeLag){
			this.timeLag = timeLag;
		}
		public void run(){
			while(!stop){
				Iterator<PlaceIt> i = prePost.values().iterator();
				
				while(i.hasNext()){
					PlaceIt pi = null;
					synchronized(i){
						pi = i.next();
					}
					if(pi.getPostDate().before(new Date())){
						i.remove();
						synchronized(onMapIterator){
							onMap.put(pi.getId(), pi);
							onMapIterator = onMap.values().iterator();
						}	
					}
				}
				try {
					sleep(timeLag);
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
		active = database.activePlaceIt();
		pulldown = database.pulldownPlaceIt();
		onMap = database.onMapPlaceIt();
		prePost = database.prepostPlaceIt();
		(nThread = new NotifyThread(NOTIFY_TIME_LAG)).start();
		(pThread = new PostThread(POST_TIME_LAG)).start();
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
		PlaceIt pi = pulldown.get(id).clone();
		pi.extendPostDate(Calendar.MINUTE, 45);
		pi.setCreateDate(new Date());
		boolean success = database.repostPlaceIt(pi);
		if(success){		
			pulldown.remove(id);
			active.put(id, pi);
		}
		return success;
	}
}
