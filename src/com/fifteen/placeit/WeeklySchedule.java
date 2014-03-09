package com.fifteen.placeit;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;



public class WeeklySchedule extends AbstractSchedule {
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

	private int repeatedDayInWeek;
	private NumOfWeekRepeat numOfWeekRepeat;
	private boolean repeatedDay[];

	public WeeklySchedule(String type, Date createDate, Date postDate, int repeatedDayInWeek, NumOfWeekRepeat numOfWeekRepeat){
		super(type, createDate, postDate);
		repeatedDay = new boolean[7];
		setRepeatedDayInWeek(repeatedDayInWeek);
		this.numOfWeekRepeat = numOfWeekRepeat;
	}

	

	public int getRepeatedDayInWeek() {
		return repeatedDayInWeek;
	}

	//parameter must be a integer of length of 7, e.g 1010001 Monday, Wednesday, Sunday 
	//the way to pass valid parameters is setRepeatedDayInWeek(PlaceIt.MON + PlaceIt.WED + PlaceIt.SUN)
	public void setRepeatedDayInWeek(int repeatedDayInWeek) {
		this.repeatedDayInWeek = repeatedDayInWeek;
		int n = 1000000;
		int r = 0;
		for(int i = 0; i < 7; i++){
			r = repeatedDayInWeek / n;
			if(r == 1)
				repeatedDay[i] = true;
			else
				repeatedDay[i] = false;
			repeatedDayInWeek = repeatedDayInWeek % n;
			n = n / 10;
		}
	}

	public boolean[] getRepeatedDay() {
		return repeatedDay;
	}



	public void setRepeatedDay(boolean[] repeatedDay) {
		this.repeatedDay = repeatedDay;
	}



	public NumOfWeekRepeat getNumOfWeekRepeat() {
		return numOfWeekRepeat;
	}

	
	public void setNumOfWeekRepeat(NumOfWeekRepeat numOfWeekRepeat) {
		this.numOfWeekRepeat = numOfWeekRepeat;
	}

	
	@Override
	public Date nextPostDate() throws ContradictoryScheduleException {
		boolean found = false;
		int earliest = 0;
		// find earliest post day in a week
		for(; earliest < 7; earliest++){
			if(found = repeatedDay[earliest])
				break;
		}
		if(!found)
			throw new ContradictoryScheduleException("set repeat by days in a week, but no weekly schedule found!");

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
	}



	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public void fillUpScheduleInfo(Map<String, String> map) {
		super.fillUpScheduleInfo(map);
		map.put(Constant.PI.REPEATED_DAY_IN_WEEK, "" + repeatedDayInWeek);
		map.put(Constant.PI.NUM_OF_WEEK_REPEAT, "" + numOfWeekRepeat);
		
	}





}
