package com.example.placeit;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.ListActivity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class PlaceItListActivity extends ListActivity 
{
	// Array list of place-its objects
	// String place holder
	private ArrayList<String> list;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		list = new ArrayList<String>();
		
		list.add("We must A");
		list.add("Everyone owes me lunch");
		list.add("This");
		list.add("is");
		list.add("a");
		list.add("test");
		list.add("Scroll bar should");
		list.add("be here by now");
		list.add("Never mind");
		list.add("My phone is big");
		list.add("So I've got to");
		list.add("Keep typing this");
		list.add("This shit");
		
		setListAdapter(new Adapter(PlaceItListActivity.this, R.layout.placeit_list_row, list));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.list, menu);
		return true;
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
				LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
