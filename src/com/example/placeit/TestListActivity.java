package com.example.placeit;

import java.util.Collection;
import java.util.Iterator;

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
import android.widget.TextView;
import android.widget.Toast;

public class TestListActivity extends Activity {

	private MyService service;
	private ServiceManager sManager;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test_list);	
		sManager = new ServiceManager(TestListActivity.this);
	}
	
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		new AsyncTask<Void, Void, Integer>(){
	        protected void onPreExecute() { }
	        protected Integer doInBackground(Void... params) {
	            while(service == null)
	            	service = sManager.bindService();
	            return new Integer(1);
	        }
	        protected void onPostExecute(Integer result) {
	        	if(service == null)
	    			Log.v("TestListActivity onStart","service is null");
	    		TextView text = (TextView)findViewById(R.id.textView1);
	    		Intent i = getIntent();
	    		boolean active = i.getBooleanExtra("active", true);
	    		Collection<PlaceIt> pis;
	    		if(active){
	    			pis = service.getActiveList();
	    			if(pis == null)
	    				Log.v("collection","collection is null");
	    		}else{
	    			pis = service.getPulldownList();
	    			if(pis == null)
	    				Log.v("collection","collection is null");
	    		}
	    		Iterator<PlaceIt> next = pis.iterator();
	    		String s = "";
	    		while(next.hasNext()){
	    			PlaceIt pi = next.next();
	    			s += pi.getId() + ": " + pi.getTitle() + "\n";
	    		}
	    		text.setText(s);
	        }
	     }.execute();
		Log.v("TestListActivity onStart","connectted to service");
	}





	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.test_list, menu);
		return true;
	}





	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		service = sManager.unBindService();
	}

}
