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
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void fillUpScheduleInfo(Map<String, String> map) {
		
	}

}
