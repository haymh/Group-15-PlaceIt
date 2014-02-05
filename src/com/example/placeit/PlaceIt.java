package com.example.placeit;
import java.util.*;


import android.text.format.Time;


public class PlaceIt {
	public static final int MON = 1000000;
	public static final int TUE = 100000;
	public static final int WED = 10000;
	public static final int THURS = 1000;
	public static final int FRI = 100;
	public static final int SAT = 10;
	public static final int SUN = 1;
	
	public enum NumOfWeekRepeat{
		ONE(1),TWO(2),THREE(3),FOUR(4);
		private int value;
		private NumOfWeekRepeat(int value){
			this.value = value;
		}
		public int getValue(){
			return value;
		}
		public static NumOfWeekRepeat genNumOfWeekRepeat(int value){
			switch(value){
			case 1:
				return ONE;
			case 2:
				return TWO;
			case 3:
				return THREE;
			case 4:
				return FOUR;
			default:
				return null;
			}
		}
	}
	
	public enum Status{
		ON_MAP(1), ACTIVE(2), PULL_DOWN(3), EXPIRED(4);
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
			case 4:
				return EXPIRED;
			default:
				return null;	
			}
		}
	}
	
	private long id;
	private String title;
	private String description;
	private boolean isRepeated;
	private int repeatedDayInWeek;
	private boolean repeatedDay[];
	private NumOfWeekRepeat numOfWeekRepeat;
	private Date createDate;
	private Date postDate;
	private Date expiration;	
	private double latitude;
	private double longitude;
	private Status status;
	
	
	
	public PlaceIt(long id, String title, String description, boolean isRepeated,
			int repeatedDayInWeek, NumOfWeekRepeat numOfWeekRepeat,
			Date createDate, Date postDate, Date expiration, Status status, double latitude,
			double longitude) {
		super();
		this.id = id;
		this.title = title;
		this.description = description;
		this.isRepeated = isRepeated;
		this.setRepeatedDayInWeek(repeatedDayInWeek);
		this.numOfWeekRepeat = numOfWeekRepeat;
		this.createDate = createDate;
		this.postDate = postDate;
		this.expiration = expiration;
		this.latitude = latitude;
		this.longitude = longitude;
		this.status = status;
	}
	public PlaceIt(String title, String description, boolean isRepeated,
			int repeatedDayInWeek, NumOfWeekRepeat numOfWeekRepeat,
			Date createDate, Date postDate, Date expiration, Status status, double latitude,
			double longitude) {
		super();
		this.title = title;
		this.description = description;
		this.isRepeated = isRepeated;
		this.setRepeatedDayInWeek(repeatedDayInWeek);
		this.numOfWeekRepeat = numOfWeekRepeat;
		this.createDate = createDate;
		this.postDate = postDate;
		this.expiration = expiration;
		this.latitude = latitude;
		this.longitude = longitude;
		this.status = status;
	}
	public long getId(){
		return id;
	}
	public void setId(long id){
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
	public boolean isRepeated() {
		return isRepeated;
	}
	public void setRepeated(boolean isRepeated) {
		this.isRepeated = isRepeated;
	}
	public int getRepeatedDayInWeek() {
		return repeatedDayInWeek;
	}
	
	//parameter must be a integer of length of 7, e.g 1010001 Monday, Wednesday, Sunday 
	//the way to pass valid parameters is setRepeatedDayInWeek(PlaceIt.MON + PlaceIt.WED + PlaceIt.SUN)
	public void setRepeatedDayInWeek(int repeatedDayInWeek) { 
		if(isRepeated){
			this.repeatedDayInWeek = repeatedDayInWeek;
			int n = 1000000;
			int r = repeatedDayInWeek / n;
			for(int i = 0; i < 7; i++){
				if(r == 1)
					repeatedDay[i] = true;
				else
					repeatedDay[i] = false;
				repeatedDayInWeek = repeatedDayInWeek % n;
				n = n / 10;
				r = repeatedDayInWeek / n;
			}
		}else{
			this.repeatedDayInWeek = 0;
			for(int i = 0; i < 7; i++)
				repeatedDay[i] = false;
		}
	}
	public NumOfWeekRepeat getNumOfWeekRepeat() {
		return numOfWeekRepeat;
	}
	public void setNumOfWeekRepeat(NumOfWeekRepeat numOfWeekRepeat) {
		this.numOfWeekRepeat = numOfWeekRepeat;
	}
	public Date getPostDate() {
		return postDate;
	}
	public void setPostDate(Date postDate) {
		this.postDate = postDate;
	}
	
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public Date getExpiration() {
		return expiration;
	}
	public void setExpiration(Date expiration) {
		this.expiration = expiration;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public Status getStatus() {
		return status;
	}
	public void setStatus(Status status) {
		this.status = status;
	}
	
	public boolean postToday() throws ContradictoryScheduleException{ 
		Date postDate = nextPostDate();
		Date today = new Date();
		if(today.getYear() == postDate.getYear() && today.getMonth() == postDate.getMonth()
				&& today.getDate() == postDate.getDate())
			return true;
		else
			return false;
	}
	
	public Date nextPostDate() throws ContradictoryScheduleException{
		if(isRepeated){
			boolean found = false;
			int earliest = 0;
			// find earliest post day in a week
			for(; earliest < 7; earliest++){
				if(found = repeatedDay[earliest])
					break;
			}
			if(!found)
				throw new ContradictoryScheduleException("set repeated, but no weekly schedule found!");
			
			Calendar c = Calendar.getInstance();
			int day = c.get(Calendar.DAY_OF_WEEK);
			int d = 0;
			switch(day){
			case Calendar.MONDAY:
				d = 0;
				break;
			case Calendar.TUESDAY:
				d = 1;
				break;
			case Calendar.WEDNESDAY:
				d = 2;
				break;
			case Calendar.THURSDAY:
				d = 3;
				break;
			case Calendar.FRIDAY:
				d = 4;
				break;
			case Calendar.SATURDAY:
				d = 5;
				break;
			case Calendar.SUNDAY:
				d = 6;
				break;
			}
			int currentWeek = c.get(Calendar.WEEK_OF_YEAR);
			c.setTime(createDate);
			int createWeek = c.get(Calendar.WEEK_OF_YEAR);
			int weekDiff = Math.abs(currentWeek - createWeek) % numOfWeekRepeat.getValue();
			c.setTime(new Date());

			found = false;
			int i = d;
			//find any post day after current day
			for(; i < 7; i++ ){
				if(found = repeatedDay[i])
					break;
			}
			if(found){
				if(weekDiff == 0)
					c.add(Calendar.DATE, i - d);
				else{
					c.add(Calendar.DATE, weekDiff * 7 - d + earliest);
				}
				return c.getTime();
			}else{
				if(weekDiff == 0)
					weekDiff = numOfWeekRepeat.getValue();
				c.add(Calendar.DATE, weekDiff * 7 - d + earliest);
				return c.getTime();
			}

		}else
			return postDate;
	}
	
	
}
