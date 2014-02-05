import java.util.*;

import android.text.format.Time;


public class PlaceIt {
	public enum NumOfWeekRepeat{
		ONE(1),TWO(2),THREE(3),FOUR(4);
		private int value;
		private NumOfWeekRepeat(int value){
			this.value = value;
		}
		public int getValue(){
			return value;
		}
	}
	
	
	private String title;
	private String description;
	private boolean isRepeated;
	private boolean repeatedDayInWeek[];
	private NumOfWeekRepeat numOfWeekRepeat;
	private Date createDate;
	private Date postDate;
	private Date expiration;	
	private double latitude;
	private double longitude;
	private int status;
	
	
	
	public PlaceIt(String title, String description, boolean isRepeated,
			boolean[] repeatedDayInWeek, NumOfWeekRepeat numOfWeekRepeat,
			Date createDate, Date postDate, Date expiration, double latitude,
			double longitude, int status) {
		super();
		this.title = title;
		this.description = description;
		this.isRepeated = isRepeated;
		this.repeatedDayInWeek = repeatedDayInWeek;
		this.numOfWeekRepeat = numOfWeekRepeat;
		this.createDate = createDate;
		this.postDate = postDate;
		this.expiration = expiration;
		this.latitude = latitude;
		this.longitude = longitude;
		this.status = status;
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
	public boolean[] getRepeatedDayInWeek() {
		return repeatedDayInWeek;
	}
	
	//parameter must be a array of size of 7
	public void setRepeatedDayInWeek(boolean[] repeatedDayInWeek) { 
		if(repeatedDayInWeek.length != 7)
			throw new IllegalArgumentException("repeatedDayInWeek needs to be 7 in length");
		this.repeatedDayInWeek = repeatedDayInWeek;
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
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
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
				if(found = repeatedDayInWeek[earliest])
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
				if(found = repeatedDayInWeek[i])
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
