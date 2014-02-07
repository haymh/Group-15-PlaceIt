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
				/*
				TextView subject = (TextView) row.findViewById(R.id.RESULTsubject);
				if(subject != null)
					subject.setText(item.getSubject());

				TextView isCreator = (TextView) row.findViewById(R.id.RESULTisCreator);
				if(isCreator != null)
				{
					if(item.getIsCreator() == true)
						isCreator.setText("Creator");
					else
						isCreator.setText("Member");
				}

				TextView dateTime = (TextView) row.findViewById(R.id.RESULTdateTime);
				dateTime.setText(item.getDate() + "       " + item.getTime());

				TextView count = (TextView) row.findViewById(R.id.RESULTcount);
				count.setText(Integer.toString(item.getCount()));

				TextView data = (TextView) row.findViewById(R.id.RESULTdata);
				data.setText(Integer.toString(number));
				*/
			}

			return row;
		}
	}

}
