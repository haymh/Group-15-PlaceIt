package com.example.placeit;

import java.util.ArrayList;

import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
 
public class TabPulledDown extends ListFragment {
 
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

		list = new ArrayList<String>();
		
		list.add("I pulled you down!");
		list.add("NA");
		
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