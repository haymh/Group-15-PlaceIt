package com.fifteen.placeit;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class MinutelySchedule extends AbstractSchedule{

	private int repeatedMinute;
	
	public MinutelySchedule(String type, Date createDate, Date postDate, int repeatedMinute){
		super(type, createDate, postDate);
		this.repeatedMinute = repeatedMinute;
	}
	@Override
	public Date nextPostDate() throws ContradictoryScheduleException {
		Date now = new Date();
		if(postDate.after(new Date()))
			return postDate;
		Calendar c = Calendar.getInstance();
		c.setTime(postDate);
		while(postDate.before(now))
			c.add(Calendar.MINUTE, repeatedMinute);
		postDate = c.getTime();
		return postDate;
	}
	@Override
	public String toString() {
		
		return null;
	}
	@Override
	public void fillUpScheduleInfo(Map<String, String> map) {
		super.fillUpScheduleInfo(map);
		map.put(Constant.PI.REPEATED_MINUTE, "" + repeatedMinute);
		
	}
	
}
