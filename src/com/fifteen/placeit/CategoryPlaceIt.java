package com.fifteen.placeit;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;

public class CategoryPlaceIt extends AbstractPlaceIt {
	private String[] categories;

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

	
	

	@Override
	public boolean trigger(LatLng currentLocation) {
		// checking if any category matched
		// store the address into map
		return false;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}



	

}
