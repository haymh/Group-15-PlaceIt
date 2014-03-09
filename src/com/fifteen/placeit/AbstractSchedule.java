package com.fifteen.placeit;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;



public abstract class AbstractSchedule {
	protected String type;
	protected Date createDate;
	protected Date postDate;
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public abstract Date nextPostDate() throws ContradictoryScheduleException ;
	public abstract String toString();
	
	
	public AbstractSchedule(String type, Date createDate, Date postDate){
		this.type = type;
		this.createDate = createDate;
		this.postDate = postDate;
	}
	
	public String getType(){
		return type;
	}
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
	
	public void fillUpScheduleInfo(Map<String, String> map){
		map.put(Constant.PI.CREATE_DATE, dateFormat.format(createDate));
		map.put(Constant.PI.POST_DATE, dateFormat.format(postDate));
	}
}
