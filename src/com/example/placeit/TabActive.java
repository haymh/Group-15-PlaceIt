package com.example.placeit;

import java.util.ArrayList;

import android.util.Log;

public class TabActive extends TabListFragment {
	
	@Override
	public void setFields() {
		imageResource = R.layout.list_active_object;
		tag = TabActive.class.getSimpleName();
	}

	@Override
	public void fillList() {
		try {
			list = new ArrayList<PlaceIt>(service.getActiveList());
		} catch(Exception e) {
			Log.wtf(tag, e);
			return;
		}
		
    	setListAdapter(new Adapter(getActivity(), imageResource, list));
	}
}