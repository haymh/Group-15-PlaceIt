package com.fifteen.placeit;

import android.location.Location;

import com.fifteen.placeit.AbstractPlaceIt.Status;
import com.google.android.gms.maps.model.LatLng;

public class LocationPlaceIt extends AbstractPlaceIt {
	private final static double RANGE = 0.8;
	private LatLng coordinate;
	private Location location;
	
	public LocationPlaceIt(long id, String title, String description, AbstractSchedule schedule, Status status, LatLng coordinate){
		super(id, title, description, schedule, status);
		this.coordinate = coordinate;
		location =  new Location("");
		location.setLatitude(coordinate.latitude);
		location.setLongitude(coordinate.longitude);
		this.placeItInfoMap.put(Constant.PI.LATITUDE, "" + coordinate.latitude);
		this.placeItInfoMap.put(Constant.PI.LONGITUDE, "" + coordinate.longitude);
	}

	@Override
	public boolean trigger(LatLng currentLocation) {
		// TODO Auto-generated method stub
		Location l = new Location("");
		l.setLatitude(currentLocation.latitude);
		l.setLongitude(currentLocation.longitude);
		return l.distanceTo(location) < RANGE;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}

	public LatLng getCoordinate() {
		return coordinate;
	}

	public void setCoordinate(LatLng coordinate) {
		this.coordinate = coordinate;
	}

}
