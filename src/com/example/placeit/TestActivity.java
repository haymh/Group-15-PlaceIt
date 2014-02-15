package com.example.placeit;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;

import com.google.android.gms.maps.model.LatLng;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class TestActivity extends Activity {
	
	

	private MyService service;
	
	//ServiceManager is managing service connection
	//when activity is destroyed, we should call service = sManager.unBindService() to disconnect
	private ServiceManager sManager;
	
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);
		sManager = new ServiceManager(this);
	}
	
	

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		/// AsyncTask to make sure service is not null
		new AsyncTask<Void, Void, Integer>(){
	        protected void onPreExecute() { }
	        protected Integer doInBackground(Void... params) {
	            while(service == null)
	            	service = sManager.bindService();
	            return new Integer(1);
	        }
	        protected void onPostExecute(Integer result) {
	             
	        }
	     }.execute();
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.test, menu);
		return true;
	}
	
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		service = sManager.unBindService();
	}

	public void generatePlaceIt(View view){
		Random r = new Random();
		service.createPlaceIt("title", "description", r.nextInt(40) < 20, r.nextInt(),
				r.nextInt(40) < 20, PlaceIt.FRI + PlaceIt.MON, PlaceIt.NumOfWeekRepeat.genNumOfWeekRepeat(r.nextInt(3) + 1),
				new Date(), new Date(), new LatLng(r.nextInt(90), r.nextInt(180)));
		Log.v("create place it","success");
	}
	
	public void gotoActive(View view){
		Intent i = new Intent(this, TestListActivity.class);
		i.putExtra("active", true);
		//Intent i = new Intent(this, PlaceItListActivity.class);
		startActivity(i);
	}
	
	public void gotoPulldown(View view){
		Intent i = new Intent(this, TestListActivity.class);
		i.putExtra("active", false);
		//Intent i = new Intent(this, PlaceItListActivity.class);
		startActivity(i);
	}
	
	public void discard(View view){
		long id = Long.parseLong(((EditText)findViewById(R.id.editId)).getText().toString());
		Log.v("TestActivity","discard");
		if(service.discardPlaceIt(id))
			Toast.makeText(TestActivity.this, id + " was discarded", Toast.LENGTH_SHORT).show();
		else
			Toast.makeText(TestActivity.this, id + " was not discarded", Toast.LENGTH_SHORT).show();
	}
	
	public void repost(View view){
		long id = Long.parseLong(((EditText)findViewById(R.id.editId)).getText().toString());
		Log.v("TestActivity","repost");
		if(service.repostPlaceIt(id))
			Toast.makeText(TestActivity.this, id + " was reposted", Toast.LENGTH_SHORT).show();
		else
			Toast.makeText(TestActivity.this, id + " was not reposted", Toast.LENGTH_SHORT).show();
	}
	
	public void pulldown(View view){
		long id = Long.parseLong(((EditText)findViewById(R.id.editId)).getText().toString());
		Log.v("TestActivity","pull down");
		if(service.pulldownPlaceIt(id))
			Toast.makeText(TestActivity.this, id + " was pulled down", Toast.LENGTH_SHORT).show();
		else
			Toast.makeText(TestActivity.this, id + " was not pulled down", Toast.LENGTH_SHORT).show();
	}

}
