package com.fifteen.placeit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Checkable;

public class CreateCatPlaceitActivity extends Activity implements AdapterView.OnItemSelectedListener {
	
	private Button cancel = null;
	private static String cat[] = {"accounting", "airport", "amusement_park", "aquarium", 
									"art_gallery", "atm","testing","testing","testing","testing"
									,"testing","testing","testing","testing","testing","testing"
									,"testing","testing","testing","testing","testing","testing"};
	private ListView listview = null;
	private CheckBox checkbox = null;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_cat_place_it);
	    
		// listview adapter 
		listview = (ListView)findViewById(R.id.cat_listview);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.cat_single_row, R.id.checkbox, cat);
		listview.setAdapter(adapter);
		
		listview.setItemsCanFocus(false);
		listview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		
		listview.setOnItemSelectedListener(this);
		
		// cancel button
		cancel = (Button)findViewById(R.id.buttonCancel);
		cancel.setOnClickListener(new cancelListener());
		
		// check box
		//checkbox = (CheckBox)findViewById(R.id.checkbox);
	}
	
	class cancelListener implements OnClickListener{

		public void onClick(View v) {			
			finish();
		}		
	}
	
	/*
	public void onclick(View view) {
		
		Log.wtf("Something", "It works");
		//CheckBox c = (CheckBox) findViewById(R.id.cat_listview);
		myFancyMethod(view);
		
	}
	
	private void myFancyMethod(View view) {
		// TODO Auto-generated method stub
		ListView temp = (ListView) findViewById(R.id.cat_listview);
		Toast.makeText(this, temp.getCount(), Toast.LENGTH_SHORT).show();
	}*/

	/*
	public boolean validate()
	{
		int count=0;
		
	}*/


	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int i,
			long l) {
		// TODO Auto-generated method stub
		Log.wtf("Something", "It works");
		//Checkable child = (Checkable) parent.getChildAt(i);
		//child.toggle();
		CheckBox temp = (CheckBox) view;
		Toast.makeText(this, temp.getText()+" is selected "+ i, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}}
