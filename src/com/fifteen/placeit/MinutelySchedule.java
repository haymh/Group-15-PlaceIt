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
		if(postDate.after(now))
			return postDate;
		Calendar c = Calendar.getInstance();
		c.setTime(postDate);
		while(postDate.before(now))
			c.add(Calendar.MINUTE, repeatedMinute);
		postDate = c.getTime();
		return postDate;
	}
	
	@Override
	public void fillUpScheduleInfo(Map<String, String> map) {
		super.fillUpScheduleInfo(map);
		map.put(Constant.PI.REPEATED_MINUTE, "" + repeatedMinute);
		
	}
	@Override
	public boolean postNowOrNot() throws ContradictoryScheduleException {
		if(postDate.before(new Date())){
			nextPostDate(); // update postDate
			return true;
		}
		return false;
	}
	
}
