package com.fifteen.placeit;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.util.Log;
import android.view.Gravity;

//Formats DetailContent
public class DetailContentFormatter {
	public static final int REGULAR_FONT = 14;
	public static final int STATUS_FONT = 10;

	public static final int SMALL_FONT = 16;
	public static final int MEDIUM_FONT = 20;
	public static final int TITLE_FONT = 40;

	private HashMap<String, String> info;
	private AbstractPlaceIt placeIt;
	private ArrayList<DetailContent> list;

	private boolean locationBased = true;

	private static final String tag = DetailContentFormatter.class.getSimpleName();

	public DetailContentFormatter(AbstractPlaceIt placeIt) {
		this.placeIt = placeIt;
		info = new HashMap<String, String>(placeIt.getPlaceItInfoMap());
		list = new ArrayList<DetailContent>();
	}

	// Formats list
	public ArrayList<DetailContent> getDetailsArray() {
		formatType();
		formatTitle();
		formatDescription();
		formatLocation();
		formatDates();
		formatRepeat();
		formatCategory();

		return list;
	}

	// Format type and status
	private void formatType() {		
		String type = "LOCATION PLACE-IT";

		if(info.get(Constant.PI.CATEGORY_ONE) != null) {
			type = "CATEGORY PLACE-IT";
			locationBased = false;
		}

		String status = info.get(Constant.PI.STATUS_NAME).toUpperCase(Locale.US);

		DetailContent detail = new DetailContent(type, status);
		detail.contentFontSize = STATUS_FONT;
		detail.contentAlignment = Gravity.RIGHT;
		detail.descriptionFontSize = STATUS_FONT;

		list.add(detail);
	}

	// Format title
	private void formatTitle() {
		list.add(new DetailContent("TITLE", info.get(Constant.PI.TITLE), TITLE_FONT));
	}

	// Format description
	private void formatDescription() {
		String description = info.get(Constant.PI.DESCRIPTION);

		if(!description.isEmpty()) {
			list.add(new DetailContent("DESCRIPTION", description, MEDIUM_FONT));
		}
	}

	// Foarmat location. Gives coordinates if it's location based. Address if it's category based
	private void formatLocation() {
		if(locationBased) {
			DecimalFormat decimal = new DecimalFormat("#.####");
			list.add(new DetailContent("LOCATION", "(" + decimal.format(placeIt.getCoordinate().latitude)
					+ ", " + decimal.format(placeIt.getCoordinate().longitude) + ")", SMALL_FONT));
		}
		else {
			String address = info.get(Constant.PI.ADDRESS);

			if( address != null && !address.isEmpty() ) {
				list.add(new DetailContent("ADDRESS", address, SMALL_FONT));
			}
		}
	}

	// Format dates
	private void formatDates() {
		Date dateCreated = placeIt.getSchedule().getCreateDate();
		Date dateToBePosted = placeIt.getSchedule().getPostDate();

		list.add(new DetailContent("DATE", dateParser(dateCreated), SMALL_FONT));

		if( !dateToBePosted.equals(dateCreated) )
			list.add(new DetailContent("DATE to be POSTED", dateParser(dateToBePosted), SMALL_FONT));
	}

	// Parses date string
	private Spanned dateParser(Date date) {
		DateFormat day = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
		DateFormat hour = new SimpleDateFormat("hh:mm aa", Locale.US);

		String parsedDate = day.format(date);
		String parsedHour = hour.format(date);

		Spanned string = Html.fromHtml("<B>" + parsedDate + "</B> " + parsedHour);
		return string;
	}

	// Format repeated schedule
	private void formatRepeat() {
		String minutes = info.get(Constant.PI.REPEATED_MINUTE);
		
		if( minutes != null) {
			list.add(new DetailContent("MINUTES to be POSTED", minutes + " minutes", SMALL_FONT));
		}
		
		String days = info.get(Constant.PI.REPEATED_DAY_IN_WEEK);
		String weeks = info.get(Constant.PI.NUM_OF_WEEK_REPEAT);

		if( days != null ) {
			weekParser(Integer.valueOf(days), weeks);//Integer.valueOf(weeks));
		}
	}

	// Parses weekly information
	private void weekParser(int daysRepeat, String weeklyRepeat) {		
		String week = "REPEAT EVERY " + weeklyRepeat + " WEEKS";

		boolean[] days = { false, false, false, false, false ,false, false };
		String daysPosted = "";
				
		int dayNumbers[] = {0, 6, 5, 4, 3, 2, 1};
		for( int i : dayNumbers ) {
			if( daysRepeat % 10 == 1) {
				days[i] = true;
			}
			daysRepeat /= 10;
		}
		
		for( int i = 0; i < 7; ++i ) {
			if( days[i] ) {
				daysPosted += "<B>" + getDayOfWeek(i) + "</B> ";
			}
			else {
				daysPosted += "<font color=#D8D8D8 >" + getDayOfWeek(i) + "</font> ";	
			}
		}
		
		Spanned string = Html.fromHtml(daysPosted);
		
		if( !daysPosted.isEmpty() )
			list.add(new DetailContent(week, string, MEDIUM_FONT));
	}
	
	// Format days of week
	private String getDayOfWeek(int day) {
		switch(day) {
		case 0:
			return "S";
		case 1:
			return "M";
		case 2:
			return "T";
		case 3:
			return "W";
		case 4:
			return "T";
		case 5:
			return "F";
		case 6:
			return "S";
		default:
			Log.wtf(tag, "Day " + String.valueOf(day) + " not found");
			return "";
		}
	}
	
	// Format the categories of categorical place its
	private void formatCategory() {
		ArrayList<String> category = new ArrayList<String>();
		
		String single;
		single = info.get(Constant.PI.CATEGORY_ONE);
		if( single != null && !single.isEmpty() ) {
			category.add(single);
		}
		
		single = info.get(Constant.PI.CATEGORY_TWO);
		if( single != null && !single.isEmpty() ) {
			category.add(single);
		}
		
		single = info.get(Constant.PI.CATEGORY_THREE);
		if( single != null && !single.isEmpty() ) {
			category.add(single);
		}
		
		if( category.size() > 0 ) {
			String categories = "";
			for(int i = 0; i < category.size(); ++i) {
				categories += category.get(i);
				
				if( i+1 < category.size() ) {
					categories += "\n";
				}
			}
			
			list.add(new DetailContent("CATEGORIES", categories, SMALL_FONT));
		}
	}
}