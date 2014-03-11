package com.fifteen.placeit;

// Constant holding class
public final class Constant {

	// SharedPreferences
	public static class SP {
		public static final String SAVE = "save";
		public static final String ZOOM = "zoom";
		public static final String LAT = "latitude";
		public static final String LNG = "longitude";
		public static final String TIME = "time";
		
		// User information
		public static class U {
			public static final String LOGIN = "logged";
			public static final String USERNAME = "username";
			public static final String PASSWORD = "password";
		}
	}

	public static final String TITLE = "Fifteen Place-It";
	public static final String REGISTER = "Register";
	public static final String LOGIN = "LOGIN";
	public static final String LOGOUT = "LOGOUT";

	// Places API 
	public static class PAPI {
		public static final String BROWSER_API = "AIzaSyAqKT5WOPokmNIKo0qeqGZyOmo76JwnJCg";
	}
	
	// Location
	public static class L {

		// Half mile in meters
		public static final int RADIUS = 805;
		
		public static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000; 
		
		// Location update intervals
		private static final int NORMAL_SECS = 5;
		private static final int FASTEST_SECS = 5;
		private static final int MPS = 1000;	// Milliseconds per second
		public static final int NORMAL_INTERVAL = NORMAL_SECS * MPS;
		public static final int FASTEST_INTERVAL = FASTEST_SECS * MPS;
		
		// Location update intervals (distance) in meters
		public static final float SMALLEST_DISTANCE_INTERVAL = 0;
		public static final float REQUEST_DISTANCE_INTERVAL = 50;
	}

	// Place-Its 
	public static class PI {
		public static final String ID = "ID";
		public static final String TITLE = "TITLE";
		public static final String DESCRIPTION = "DESCRIPTION";
		public static final String REPEATED_DAY_IN_WEEK = "REPEATED_DAY_IN_WEEK";
		public static final String REPEATED_MINUTE = "REPEATED_MINUTE";
		public static final String NUM_OF_WEEK_REPEAT = "NUM_OF_WEEK_REPEAT";
		public static final String CREATE_DATE = "CREATE_DATE";
		public static final String POST_DATE = "POST_DATE";
		public static final String LATITUDE = "LATITUDE";
		public static final String LONGITUDE = "LONGITUDE";
		public static final String STATUS = "STATUS";
		public static final String STATUS_NAME = "STATUS_NAME";
		public static final String CATEGORY_ONE = "CATEGORY_ONE";
		public static final String CATEGORY_TWO = "CATEGORY_TWO";
		public static final String CATEGORY_THREE = "CATEGORY_THREE";
		public static final String REPEATED = "REPEATED";
		public static final String ONE_TIME = "ONE_TIME";
		public static final String ADDRESS = "ADDRESS";
		public static final int NUM_OF_CATEGORIES = 3;
	}
	
	// GCM constants
	public static class GCM{
		/**
	     * Google API project id registered to use GCM.
	     */
	    public static final String SENDER_ID = "592230020068";
	}
}
