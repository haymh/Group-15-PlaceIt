package com.fifteen.placeit;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;



public abstract class AbstractSchedule {
	protected Date createDate;
	protected Date postDate;
	
	public abstract Date nextPostDate() throws ContradictoryScheduleException ;
	public abstract String toString();
	public abstract void fillUpScheduleInfo(Map<String, String> map);
	
	
	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Date getPostDate() {
		return postDate;
	}

	public void setPostDate(Date postDate) {
		this.postDate = postDate;
	}

	// extend number of unit time from current time
	public void extendPostDate(int unit, int number){
		Calendar c = Calendar.getInstance();
		c.add(unit, number);
		this.setPostDate(c.getTime());
	}
	
	
	// return if we should post it right now
	public boolean postNowOrNot() throws ContradictoryScheduleException{
		Date postDate = nextPostDate();
		Date today = new Date();
		if(today.getYear() == postDate.getYear() && today.getMonth() == postDate.getMonth()
				&& today.getDate() == postDate.getDate())
			return true;
		else
			return false;
	}
}
