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
	
	private ServiceConnection connection = new ServiceConnection(){

		@Override
		public void onServiceConnected(ComponentName arg0, IBinder arg1) {
			// TODO Auto-generated method stub
			service = ((MyService.LocalBinder)arg1).getService();
			Toast.makeText(TestListActivity.this, "connnected to service", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			// TODO Auto-generated method stub
			service = null;
			Toast.makeText(TestListActivity.this, "disconnnected from service", Toast.LENGTH_SHORT).show();
		}
		
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test_list);
		bindService(new Intent(TestListActivity.this, MyService.class), connection, Context.BIND_AUTO_CREATE);
		new AsyncTask<Void, Void, Integer>(){
	        protected void onPreExecute() { }
	        protected Integer doInBackground(Void... params) {
	            while(service == null);
	            return new Integer(1);
	        }
	        protected void onPostExecute(Integer result) {
	             // service is up, m_SrvConnection is set
	             // what you wanted to do with the service in onCreate() goes here
	        	if(service == null)
	    			Log.v("TestListActivity onStart","service is null");
	    		TextView text = (TextView)findViewById(R.id.textView1);
	    		Collection<PlaceIt> pis = service.getActiveList();
	    		Iterator<PlaceIt> next = pis.iterator();
	    		String s = "";
	    		while(next.hasNext()){
	    			PlaceIt pi = next.next();
	    			s = pi.getId() + ": " + pi.getTitle() + "\n";
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

}
