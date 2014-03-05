package com.fifteen.placeit;

import java.util.ArrayList;

import com.fifteen.placeit.R;

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
			list = new ArrayList<PlaceIt>(service.getPrepostList());
		} catch(Exception e) {
			Log.wtf(tag, e);
		}
		
		try {
			list.addAll(service.getOnMapList());
		} catch(Exception e) {
			try {
				list = new ArrayList<PlaceIt>(service.getOnMapList());
			} catch(Exception e2) {
				Log.wtf(tag + " inner", e2);
				return;
			}
			Log.wtf(tag, e);
			return;
		}
		
    	setListAdapter(new Adapter(getActivity(), imageResource, list));
	}
}