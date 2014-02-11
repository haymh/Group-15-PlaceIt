package com.example.placeit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import android.app.ListFragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
 
public class TabActive extends ListFragment {
	
	private MyService service;
	private Collection<PlaceIt> placeIts;
	

	
 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 
        return (LinearLayout) inflater.inflate(R.layout.tab_active, container, false);
    }
    
	// Array list of place-its objects
	// String place holder
	private ArrayList<String> list;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) 
	{
		super.onActivityCreated(savedInstanceState);
		//service = ((PlaceItListActivity)getActivity()).getService();
		list = new ArrayList<String>();
		if(service == null)
			Log.v("activeTab oncreate","service is null");
		else
			Log.v("activeTab oncreate","service is not null");
		placeIts = service.getActiveList();
		Log.v("activeTab oncreate","got active list");
		if(!placeIts.isEmpty()){
			Iterator<PlaceIt> nextPlaceIt = placeIts.iterator();
			while(nextPlaceIt.hasNext()){
				list.add(nextPlaceIt.next().getTitle());
			}
		}
		
		
		list.add("first");
		Log.v("activetab","active tab is built");
		
		setListAdapter(new Adapter(this.getActivity(), R.layout.placeit_list_row, list));
	}
	

	public class Adapter extends ArrayAdapter<String>
	{
		public Adapter(Context context, int textResource, ArrayList<String> objects)
		{
			super(context, textResource, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) 
		{			
			View row = convertView;

			if(row == null)
			{	
				LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				row = inflater.inflate(R.layout.placeit_list_row, null);
			}

			String item = getItem(position);

			if(item != null)
			{
				
				TextView title = (TextView) row.findViewById(R.id.inListTitle);
				if(title != null)
					title.setText(item);
	
				TextView detail = (TextView) row.findViewById(R.id.inListDetail);
				if(detail != null)
					detail.setText("Some very specific detail goes in here, I think");
				
			}

			return row;
		}
	}

 
}