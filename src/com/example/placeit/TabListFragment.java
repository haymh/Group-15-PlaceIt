package com.example.placeit;

import java.util.List;

import android.app.ListFragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
 
public abstract class TabListFragment extends ListFragment {
	protected ServiceManager manager;
	protected MyService service;
	
	protected List<PlaceIt> list;
	
	protected int imageResource;
	protected String tag;

//FRAGMENT DEFINITIONS		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setFields();
		
		manager = new ServiceManager(getActivity());
	}
	
	// Allows inherited classes to set fields
	public abstract void setFields();
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	super.onCreateView(inflater, container, savedInstanceState);
    	
        return (LinearLayout) inflater.inflate(R.layout.tab_list_fragment, container, false);
    }
	
    // Binds service, if service bound, pull data by calling filllist()
    public void onResume() {
        super.onResume();
        
        new AsyncTask<Void, Void, Integer>() {
	        protected void onPreExecute() {}

	        protected Integer doInBackground(Void... params) {
	            while(service == null)
	            	service = manager.bindService();
	            return Integer.valueOf(1);
	        }
	        
	        protected void onPostExecute(Integer result) {
	        	if(service == null) {
	        		Log.wtf(tag, "No service");
	        		return;
	        	}

	        	try {
	        		service.getActiveList();
	        	}
	        	catch(Exception e) {
	        		Log.wtf(tag, e);
	        		return;
	        	}
	        	
	        	fillList();
	        }
	    }.execute();
    }    
    
    public abstract void fillList();
    
    @Override
    public void onDetach() {
    	service = manager.unBindService();
    	
    	super.onDetach();
    }

//UI DEFINITIONS
	public class Adapter extends ArrayAdapter<PlaceIt> {
		
		public Adapter(Context context, int textResource, List<PlaceIt> objects) {
			super(context, textResource, objects);
		}

		// Fills the list fragment
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;

			if(row == null) {
				LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				row = inflater.inflate(imageResource, null);
			}

			PlaceIt item = getItem(position);

			// Sets text into respective TextView
			if(item != null) {
				TextView title = (TextView) row.findViewById(R.id.inListTitle);
				if(title != null)
					title.setText(item.getTitle());

				TextView detail = (TextView) row.findViewById(R.id.inListDetail);
				if(detail != null)
					detail.setText("ID: " + item.getId());
				
				TextView id = (TextView) row.findViewById(R.id.inListID);
				if(id != null)
					id.setText("" + item.getId());
			}
			return row;
		}
	}
}