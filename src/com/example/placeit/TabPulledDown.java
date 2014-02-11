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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
 
public class TabPulledDown extends ListFragment {
 
	private MyService service;
	private Collection<PlaceIt> placeIts;
	
	private ServiceConnection connection = new ServiceConnection(){

		@Override
		public void onServiceConnected(ComponentName arg0, IBinder arg1) {
			// TODO Auto-generated method stub
			service = ((MyService.LocalBinder)arg1).getService();
			Toast.makeText(getActivity(), "connnected to service", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			// TODO Auto-generated method stub
			service = null;
			Toast.makeText(getActivity(), "disconnnected from service", Toast.LENGTH_SHORT).show();
		}
		
	};
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 
        return (LinearLayout) inflater.inflate(R.layout.tab_active, container, false);
    }
    
	// Array list of place-its objects
	// String place holder
	private ArrayList<String> list;
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		getActivity().bindService(new Intent(getActivity(), MyService.class), connection, Context.BIND_AUTO_CREATE);
		list = new ArrayList<String>();
		placeIts = service.getPulldownList();
		if(!placeIts.isEmpty()){
			Iterator<PlaceIt> nextPlaceIt = placeIts.iterator();
			while(nextPlaceIt.hasNext()){
				list.add(nextPlaceIt.next().getTitle());
			}
		}
		
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
					detail.setText("Expired");
				
			}

			return row;
		}
	}
}