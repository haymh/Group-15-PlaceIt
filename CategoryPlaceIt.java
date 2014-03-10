package com.fifteen.placeit;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

// Subclass of the abstract superclass place it
// Category place it definition
public class CategoryPlaceIt extends AbstractPlaceIt {
	private String[] categories;
	
	private static final String tag = CategoryPlaceIt.class.getSimpleName();

	public CategoryPlaceIt(long id, String title, String description,
			AbstractSchedule schedule, Status status, String[] categories) {
		super(id, title, description, schedule, status);
		coordinate = null;
		this.categories = categories;
		int length = categories.length;
		switch(length){
		case 3:
			this.placeItInfoMap.put(Constant.PI.CATEGORY_THREE, categories[2]);
		case 2:
			this.placeItInfoMap.put(Constant.PI.CATEGORY_TWO, categories[1]);
		case 1:
			this.placeItInfoMap.put(Constant.PI.CATEGORY_ONE, categories[0]);
		}
	}

	// Handles category place it specific implementation
	// Gets data from the saved JSON from Places API
	@Override
	public boolean trigger(LatLng currentLocation) {
		String address;
		
		for(int i = 0; i < categories.length; ++i) {
			address = new JSONParser().getAddress(categories[i]);
			
			// TODO Debug mode
			if( address != null) {
				Log.wtf(tag, "trigger() " + address);
				if( !address.isEmpty() ) { // TODO attach back to first if statement
					placeItInfoMap.put(Constant.PI.ADDRESS, address);
					status = AbstractPlaceIt.Status.PULL_DOWN;
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}



	

}
