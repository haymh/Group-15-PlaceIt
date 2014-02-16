package com.example.placeit;

import java.util.ArrayList;

public class TabPulledDown extends TabListFragment {

	@Override
	public void setFields() {
		imageResource = R.layout.list_pulled_object;
		tag = TabPulledDown.class.getSimpleName();
	}

	@Override
	public void fillList() {
		list = new ArrayList<PlaceIt>(service.getPulldownList());
    	setListAdapter(new Adapter(getActivity(), imageResource, list));
	}


}