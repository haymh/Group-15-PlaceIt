package com.fifteen.placeit;

import java.util.Date;
import java.util.Map;

public class OneTimeSchedule extends AbstractSchedule {
	
	public OneTimeSchedule(String type, Date createDate, Date postDate){
		super(type, createDate, postDate);
	}

	@Override
	public Date nextPostDate() throws ContradictoryScheduleException {
		return postDate;
	}


	@Override
	public void fillUpScheduleInfo(Map<String, String> map) {
		super.fillUpScheduleInfo(map);
	}

	@Override
	public boolean postNowOrNot() throws ContradictoryScheduleException {

		return postDate.compareTo(new Date()) <= 0;
	}

}
