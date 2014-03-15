package com.fifteen.placeit;

import java.util.Date;
import java.util.Random;

import com.fifteen.placeit.R;
import com.google.android.gms.maps.model.LatLng;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
 
public class PlaceItListActivity extends Activity {
	private static String TAG = PlaceItListActivity.class.getSimpleName();
	
	private MyService service;
	private ServiceManager manager;
	private Fragment currentFragment;
	private long placeItId;
	
//ACTIVITY DEFINITIONS
	
    // Creates tabs and corresponding lists
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 
        String activeLabel = "Active", pulledDownLabel = "Pulled Down";
        
        Tab tab = actionBar.newTab();
        tab.setText(activeLabel);
        TabListener<TabActive> activeTab = new TabListener<TabActive>(this, activeLabel, TabActive.class);
        tab.setTabListener(activeTab);
        actionBar.addTab(tab);
        
        tab = actionBar.newTab();
        tab.setText(pulledDownLabel);
        TabListener<TabPulledDown> pullDownTab = new TabListener<TabPulledDown>(this, pulledDownLabel, TabPulledDown.class);
        tab.setTabListener(pullDownTab);
        actionBar.addTab(tab);
        
        manager = new ServiceManager(this);
    }
    
    // Binds service
    public void onResume(){
    	super.onResume();
    	
        new AsyncTask<Void, Void, Integer>() {
	        protected void onPreExecute() {}

	        protected Integer doInBackground(Void... params) {
	            while(service == null)
	            	service = manager.bindService();
	            return Integer.valueOf(1);
	        }
	        
	        protected void onPostExecute(Integer result) { }
	    }.execute();
    }
    
    public void onDestroy(){
    	service = manager.unBindService();
    	super.onDestroy();
    }
    
//FRAGMENT & BUTTON HANDLERS
    
    // List object clicked, go to detail page
    public void gotoDetailPage(View view) {
    	getPlaceItId(view);
    	
    	Intent i = new Intent(this, PlaceItDetailActivity.class);
    	i.putExtra("id", placeItId);
    	startActivity(i);
    }
    
    // Repost button clicked, repost placeit
    public void repostPlaceIt(View view) {
    	View parent = (View) view.getParent();
    	view.setEnabled(false);
    	
    	getPlaceItId(parent);
    	if(service != null) {
			final ProgressDialog dialog = ProgressDialog.show(this,
					"reposting...", "Please wait...", false);
			new AsyncTask<Void,Void,Boolean>(){

				@Override
				protected Boolean doInBackground(Void... arg0) {
					return service.repostPlaceIt(placeItId);
				}
				
				@Override
				protected void onPostExecute(Boolean params) {
					dialog.dismiss();
					if(params){
						Toast.makeText(PlaceItListActivity.this, "Place-it has been reposted", Toast.LENGTH_SHORT).show();
						fragmentRefresh();
					}else
						Toast.makeText(PlaceItListActivity.this, "cannot access server, place-it was not reposted", Toast.LENGTH_SHORT).show();
				}
				
			}.execute();
			dialog.show();		
		}else{
			Toast.makeText(PlaceItListActivity.this, "Unable to perform your request at this moment", Toast.LENGTH_SHORT).show();
		}
    	
    	
    }
    
    // Pull down button clicked, pull down placeit
    public void pullDownPlaceIt(View view) {
    	View parent = (View) view.getParent();
    	view.setEnabled(false);
    	
    	getPlaceItId(parent);
    	
    	if(service != null) {
			final ProgressDialog dialog = ProgressDialog.show(this,
					"Pulling Down...", "Please wait...", false);
			new AsyncTask<Void,Void,Boolean>(){

				@Override
				protected Boolean doInBackground(Void... arg0) {
					return service.pulldownPlaceIt(placeItId);
				}
				
				@Override
				protected void onPostExecute(Boolean params) {
					dialog.dismiss();
					if(params){
						Toast.makeText(PlaceItListActivity.this, "Place-it has been pulled down", Toast.LENGTH_SHORT).show();
						fragmentRefresh();
					}else
						Toast.makeText(PlaceItListActivity.this, "cannot access server, place-it was not pulled down", Toast.LENGTH_SHORT).show();
				}
				
			}.execute();
			dialog.show();		
		}else{
			Toast.makeText(PlaceItListActivity.this, "Unable to perform your request at this moment", Toast.LENGTH_SHORT).show();
		}
    }
    
    // Discard button clicked, discards placeit
    // Same behavior for both active and pull down lists
    public void discardPlaceIt(View view) {
    	View parent = (View) view.getParent();
    	view.setEnabled(false);
    	
    	getPlaceItId(parent);
    	if(service != null) {
			final ProgressDialog dialog = ProgressDialog.show(this,
					"discarding...", "Please wait...", false);
			new AsyncTask<Void,Void,Boolean>(){

				@Override
				protected Boolean doInBackground(Void... arg0) {
					return service.discardPlaceIt(placeItId);
				}
				
				@Override
				protected void onPostExecute(Boolean params) {
					dialog.dismiss();
					if(params){
						Toast.makeText(PlaceItListActivity.this, "Place-it has been discarded", Toast.LENGTH_SHORT).show();
						fragmentRefresh();
					}else
						Toast.makeText(PlaceItListActivity.this, "cannot access server, place-it was not discarded", Toast.LENGTH_SHORT).show();
				}
				
			}.execute();
			dialog.show();		
		}else{
			Toast.makeText(PlaceItListActivity.this, "Unable to perform your request at this moment", Toast.LENGTH_SHORT).show();
		}
    	
    	
    }
    
//HANDLERS SUPPORT methods
    
    // Gets placeit ID to be used for pull down, discard or repost
    // Uses ID to access service
    public void getPlaceItId(View view) {
    	TextView id = (TextView) view.findViewById(R.id.inListID);
    	placeItId = Long.valueOf( (String) id.getText() );
    }
    
    // Refresh current fragment
    public void fragmentRefresh() {
    	currentFragment.getFragmentManager().findFragmentByTag("FRAGMENT");
    	currentFragment.onResume();
    }
 
//UI DEFINITION, for UI only
    
    // Tab listener, changes tab on tap
    private class TabListener<T extends Fragment> implements ActionBar.TabListener {
        private Fragment mFragment;
        private final Activity mActivity;
        private final String mTag;
        private final Class<T> mClass;
 
        public TabListener(Activity activity, String tag, Class<T> clz) {
            mActivity = activity;
            mTag = tag;
            mClass = clz;
        }
 
        // Attaches fragment if respective tab is selected
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
            if (mFragment == null) {
                mFragment = Fragment.instantiate(mActivity, mClass.getName());
                ft.add(android.R.id.content, mFragment, mTag);
            } else {
                ft.attach(mFragment);
            }
            
            currentFragment = mFragment;
        }

		// Detaches fragment on unselect
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
            if (mFragment != null)
                ft.detach(mFragment);
        }
 
        public void onTabReselected(Tab tab, FragmentTransaction ft) { }
    }
}
