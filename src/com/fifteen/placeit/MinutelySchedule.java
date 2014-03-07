package com.fifteen.placeit;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class MinutelySchedule extends AbstractSchedule{

	private int repeatedMinute;
	
	public MinutelySchedule(int repeatedMinute, Date postDate){
		this.repeatedMinute = repeatedMinute;
		this.postDate = postDate;
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
		map.put(Constant.PI.REPEATED_MINUTE, "" + repeatedMinute);
		
	}
	
}
