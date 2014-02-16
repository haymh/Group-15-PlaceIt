package com.example.placeit;

import java.util.ArrayList;

public class TabActive extends TabListFragment {
	
	@Override
	public void setFields() {
		imageResource = R.layout.list_active_object;
		tag = TabActive.class.getSimpleName();
	}

	@Override
	public void fillList() {
		list = new ArrayList<PlaceIt>(service.getActiveList());
    	setListAdapter(new Adapter(getActivity(), imageResource, list));
	}
}