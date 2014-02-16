package com.example.placeit;

import java.util.ArrayList;

import android.util.Log;

public class TabPulledDown extends TabListFragment {

	@Override
	public void setFields() {
		imageResource = R.layout.list_pulled_object;
		tag = TabPulledDown.class.getSimpleName();
	}

	@Override
	public void fillList() {
		try {
			list = new ArrayList<PlaceIt>(service.getPulldownList());
		} catch(Exception e) {
			Log.wtf(tag, e);
			return;
		}
		
    	setListAdapter(new Adapter(getActivity(), imageResource, list));
	}


}