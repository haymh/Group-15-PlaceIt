package com.fifteen.placeit;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;




public abstract class AbstractPlaceIt {
	
	public enum Status{
		ON_MAP(1), ACTIVE(2), PULL_DOWN(3);
		private int value;
		private Status(int value){
			this.value = value;
		}
		public int getValue(){
			return value;
		}
		public static Status genStatus(int value){
			switch(value){
			case 1:
				return ON_MAP;
			case 2:
				return ACTIVE;
			case 3:
				return PULL_DOWN;
			default:
				return null;	
			}
		}
		public String toString(){
			switch(value){
			case 1:
				return "On Map";
			case 2:
				return "Active";
			case 3:
				return "Pull Down";
			default:
				return "Unknown";
			}
		}
	}
	
	
	protected long id;
	protected String title;
	protected String description;
	protected AbstractSchedule schedule;
	protected Status status;
	protected Map<String,String> placeItInfoMap;
	protected LatLng coordinate;
	
	
	public AbstractPlaceIt(long id, String title, String description,
			AbstractSchedule schedule, Status status) {
		super();
		this.id = id;
		this.title = title;
		this.description = description;
		this.schedule = schedule;
		this.status = status;
		placeItInfoMap = new HashMap<String,String>();
		placeItInfoMap.put(Constant.PI.ID, "" + id);
		placeItInfoMap.put(Constant.PI.TITLE, title);
		placeItInfoMap.put(Constant.PI.DESCRIPTION, description);
		placeItInfoMap.put(Constant.PI.STATUS, status.toString());
		placeItInfoMap.put(Constant.PI.CREATE_DATE, schedule.getCreateDate().toString());
		placeItInfoMap.put(Constant.PI.POST_DATE, schedule.getPostDate().toString());
		schedule.fillUpScheduleInfo(placeItInfoMap);
	}
	public LatLng getCoordinate() {
		return coordinate;
	}
	public void setCoordinate(LatLng coordinate) {
		this.coordinate = coordinate;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public AbstractSchedule getSchedule() {
		return schedule;
	}
	public void setSchedule(AbstractSchedule schedule) {
		this.schedule = schedule;
	}
	public Status getStatus() {
		return status;
	}
	public void setStatus(Status status) {
		this.status = status;
	}
	public Map<String, String> getPlaceItInfoMap(){
		return this.placeItInfoMap;
	}
	
	
	public abstract boolean trigger(LatLng currentLocation);
	
	public abstract String toString();
	
	
	
	
}
