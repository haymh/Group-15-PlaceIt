package com.fifteen.placeit;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.android.gms.maps.model.LatLng;

public class PlaceItFactory {
	
	public static AbstractPlaceIt createPlaceIt(long id, String title, String description, int repeatedDayInWeek, int repeatedMinute, 
			WeeklySchedule.NumOfWeekRepeat numOfWeekRepeat, Date createDate, Date postDate, double latitude, double longitude,
			AbstractPlaceIt.Status status, String[] categories){
		AbstractPlaceIt pi;
		AbstractSchedule schedule;
		if(repeatedDayInWeek != 0)
			schedule = new WeeklySchedule(Constant.PI.REPEATED, createDate, postDate, repeatedDayInWeek, numOfWeekRepeat);
		else if(repeatedMinute != 0)
			schedule = new MinutelySchedule(Constant.PI.REPEATED, createDate, postDate, repeatedMinute);
		else
			schedule = new OneTimeSchedule(Constant.PI.ONE_TIME, createDate, postDate);
		if(categories != null && categories.length > 0)
			pi = new CategoryPlaceIt(id, title, description, schedule, status, categories);
		else
			pi = new LocationPlaceIt(id, title, description, schedule, status, latitude, longitude);
		return pi;
	}
	
	public static AbstractPlaceIt createPlaceIt(long id, String title, String description, int repeatedDayInWeek, int repeatedMinute, 
			WeeklySchedule.NumOfWeekRepeat numOfWeekRepeat, String createDate, String postDate, double latitude, double longitude,
			AbstractPlaceIt.Status status, String[] categories){
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try{
			return PlaceItFactory.createPlaceIt(id, title, description, repeatedDayInWeek, repeatedMinute, numOfWeekRepeat,
					dateFormat.parse(createDate), dateFormat.parse(postDate), latitude, longitude, status, categories);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
}
