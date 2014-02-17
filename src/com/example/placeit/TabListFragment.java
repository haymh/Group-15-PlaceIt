package com.example.placeit;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
	        	
	        	fillList();
	        }
	    }.execute();
    }    
    
    public abstract void fillList();
    
    @Override
    public void onDestroy() {
    	service = manager.unBindService();
    	super.onDestroy();
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
				if(detail != null) {
					
					int status = item.getStatus().getValue();
					String message = "";
					
					// Place It was pulled down, display posted date
					// Place It is active, post type
					switch(status) {
					case 1:
						message = "ON MAP";
						break;
					case 2:
						message = "TO BE POSTED";
						break;
					case 3:
						message = dateParser(item.getCreateDate());
						break;
					default:
						Log.wtf(tag, "Bad Place It Status " + status);
						message = "FUNNY ERROR";
					}
					detail.setText(message);
				}
				
				TextView id = (TextView) row.findViewById(R.id.inListID);
				if(id != null)
					id.setText("" + item.getId());
			}
			return row;
		}
		
		private String dateParser(Date date) {
			DateFormat day = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
			
			String parsedDate = day.format(date);
			
			return "DATE POSTED " + parsedDate;
		}
	}
}