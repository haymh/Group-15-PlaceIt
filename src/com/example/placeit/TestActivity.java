package com.example.placeit;

import java.util.Date;
import java.util.Random;

import com.google.android.gms.maps.model.LatLng;

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
import android.widget.Toast;

public class TestActivity extends Activity {
	
	private MyService service;
	
	private ServiceConnection connection = new ServiceConnection(){

		@Override
		public void onServiceConnected(ComponentName arg0, IBinder arg1) {
			// TODO Auto-generated method stub
			service = ((MyService.LocalBinder)arg1).getService();
			Toast.makeText(TestActivity.this, "connnected to service", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			// TODO Auto-generated method stub
			service = null;
			Toast.makeText(TestActivity.this, "disconnnected from service", Toast.LENGTH_SHORT).show();
		}
		
	};
	
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);
		bindService(new Intent(TestActivity.this, MyService.class), connection, Context.BIND_AUTO_CREATE);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.test, menu);
		return true;
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		this.unbindService(connection);
		service = null;
	}

	public void generatePlaceIt(View view){
		Random r = new Random();
		service.createPlaceIt("title", "description", r.nextInt(40) < 20, r.nextInt(),
				r.nextInt(40) < 20, PlaceIt.FRI + PlaceIt.MON, PlaceIt.NumOfWeekRepeat.genNumOfWeekRepeat(r.nextInt(3) + 1),
				new Date(), new Date(), new LatLng(r.nextInt(90), r.nextInt(180)));
		Log.v("create place it","success");
	}
	
	public void gotoList(View view){
		Intent i = new Intent(this, TestListActivity.class);
		startActivity(i);
	}

}
