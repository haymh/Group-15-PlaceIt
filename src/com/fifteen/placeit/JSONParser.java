package com.fifteen.placeit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

//Parses JSON then puts them on lists and maps for easy access
public class JSONParser {
	private static final String tag = JSONParser.class.getSimpleName();

	// Places API map. <Type, Address>
	private static HashMap<String, String> placesAPIMap = new HashMap<String, String>();

	// Place It info map. List<Place Its>
	// Map<field, content>
	private static List<Map<String, String>> placeItInfoList = new ArrayList<Map<String, String>>();
	
	// Place Id id and status list. 
	private static List<StatusObject> placeItIdStatusList = new ArrayList<StatusObject>();

	// Place It id list List<id>
	private static List<Long> placeItIdList = new ArrayList<Long>();
	
	// Place It init list List<Place Its>
	// Map<field, content>
	private static List<Map<String, String>> placeItInitList = new ArrayList<Map<String, String>>();

	private static Long time;
	
	private static JSONParser parser = new JSONParser();
	
//PUBLIC STATUS HOLDER CLASS
	public class StatusObject {
		public final long id;
		public final int status;
		
		public StatusObject(long id, int status) {
			this.id = id;
			this.status = status;
		}
	}

//PLACES API PARSE & SUPPORT
	// Parses JSON by Places API format
	public static void parsePlacesAPI(String data) {
		placesAPIMap = new HashMap<String, String>();

		try {
			JSONObject object = new JSONObject(data);
			JSONArray array = new JSONArray(object.getString("results"));
			setupPlacesAPIMap(array);
		} catch (Exception e) {
			Log.wtf(tag, "parsePlacesAPI()\n" + e.toString());
		}
	}

	// Set ups places access map
	private static void setupPlacesAPIMap(JSONArray array) {
		String address;

		for(int i = 0; i < array.length(); ++i) {
			try {
				JSONObject single = array.getJSONObject(i);
				address = single.getString("vicinity");

				// Stores address into the given type
				JSONArray types = new JSONArray(single.getString("types"));
				for(int t = 0; t < types.length(); ++t) {
					placesAPIMap.put(types.getString(t), address);
				}
			} catch (JSONException e) {
				Log.wtf(tag, "setupPlacesAPIMap()\n" + e.toString());
			}
		}
	}

	public static String getAddress(String category) {
		return placesAPIMap.get(category);
	}

// PLACES IT SERVER PARSE + SUPPORT
	// Parse Place It server 
	public static void parsePlaceItServer(String data) {
		placeItInfoList = new ArrayList<Map<String, String>>();
		placeItIdStatusList = new ArrayList<StatusObject>();
		placeItIdList = new ArrayList<Long>();
		
		// TODO Debug, get this one out
		Log.wtf("SERVER O JSON string", data);
		
		try {
			JSONObject object = new JSONObject(new JSONObject(data).getString("operation"));

			try {
				time = new JSONObject(object.getString("time")).getLong("time");
			} catch(Exception e) {
				time = Long.valueOf(-1);
			}

			setupPlaceItInfoList(object.optString("create"));
			setupPlaceItIdStatusMap(object.optString("change"));
			setupPlaceItIdList(object.optString("delete"));

		}catch (Exception e) {
			Log.wtf("parsePlaceItServer()", e.toString());
		} 
	}

	// Parses place it list
	private static void setupPlaceItInfoList(String data) {		
		JSONArray array;

		try {
			array = new JSONArray(new JSONObject(data).getString("placeit"));
		}catch(JSONException e) {
			Log.wtf("setupPlaceItInfoList()", "Bad O string " + e.toString());
			return;
		}

		for( int i = 0; i < array.length(); ++i ) {
			try {
				Map<String, String> temp = getPlaceIt(array.getJSONObject(i));
				
				if(temp != null) {
					placeItInfoList.add(temp);
				}		
			}catch(JSONException e) {
				Log.wtf("setupPlaceItInfoList()", "Bad data " + e.toString());
			}
		}
	}

	// Parses id + status map
	private static void setupPlaceItIdStatusMap(String data) {
		try {
			JSONArray array = new JSONArray(new JSONObject(data).getString("placeit"));

			for( int i = 0; i < array.length(); ++i ) {
				JSONObject single = array.getJSONObject(i);
				StatusObject status = parser.new StatusObject(single.getLong(Constant.PI.ID), single.getInt(Constant.PI.STATUS));
				
				placeItIdStatusList.add(status);
			}
		} catch(Exception e) {
			Log.wtf("setupPlaceItIdStatusMap() ", e.toString());
		}
	}

	// Parses id list
	private static void setupPlaceItIdList(String data) {
		try {
			JSONArray ids = new JSONArray(new JSONObject(data).optString("placeit"));

			for(int i = 0; i < ids.length(); ++i) {
				JSONObject single = ids.getJSONObject(i);	
				placeItIdList.add(single.getLong(Constant.PI.ID));
			}
		} catch(Exception e) {
			Log.wtf("setupPlaceItIdList()", e.toString());
		}
	}

	// Pass place it info map (list of place its)
	public static List<Map<String, String>> getPlaceItInfoList() {
		return placeItInfoList;
	}

	// Pass place it id and status
	public static List<StatusObject> getPlaceItIdStatusList() {
		return placeItIdStatusList;
	}

	// Pass place it id lists
	public static List<Long> getPlaceItIdList() {
		return placeItIdList;
	}
	
	// Pass back time
	public static Long getTime() {
		return time;
	}
	
// PLACE IT SERVER INIT
	// Parse Place It server init 
	public static void parsePlaceItInit(String data) {
		placeItInitList = new ArrayList<Map<String, String>>();
		
		// Debug get this one out
		Log.wtf("INIT O JSON string", data);
		
		try {
			JSONObject object = new JSONObject(data);

			try {
				time = new JSONObject(object.getString("time")).getLong("time");
			} catch(Exception e) {
				time = Long.valueOf(-1);
			}

			setupPlaceItInitList(object.getString("data"));
		} catch (Exception e) {
			Log.wtf("parsePlaceItInit()", e.toString());
		}
	}

	// Parses place it init list
	private static void setupPlaceItInitList(String data) {		
		JSONArray array;

		try {
			array = new JSONArray(data);
		}catch(JSONException e) {
			Log.wtf("setupPlaceItInitList()", "Bad O string " + e.toString());
			return;
		}

		for( int i = 0; i < array.length(); ++i ) {
			try {
				Map<String, String> temp = getPlaceIt(array.getJSONObject(i));
				
				if(temp != null) {
					placeItInitList.add(temp);
				}
			}catch(JSONException e) {
				Log.wtf("setupPlaceItInitList()", "Bad data " + e.toString());
			}
		}
	}
	
	// Pass place it init list (list of place its)
	public static List<Map<String, String>> getPlaceItInitList() {
		return placeItInitList;
	}
	
// PLACE IT EXTRACTOR
	// Get individual place it from JSON
	private static Map<String, String> getPlaceIt(JSONObject single) {
		Map<String, String> placeIt = new HashMap<String, String>();
		
		try {
			placeIt.put(Constant.PI.ID, single.getString(Constant.PI.ID));
			placeIt.put(Constant.PI.CREATE_DATE, single.getString(Constant.PI.CREATE_DATE));
			placeIt.put(Constant.PI.POST_DATE, single.getString(Constant.PI.POST_DATE));
			placeIt.put(Constant.PI.TITLE, single.getString(Constant.PI.TITLE));
			placeIt.put(Constant.PI.STATUS, single.getString(Constant.PI.STATUS));

			String location = single.optString(Constant.PI.LATITUDE);
			if( !location.isEmpty() ) {
				// It's a location place it
				placeIt.put(Constant.PI.LATITUDE, location);
				placeIt.put(Constant.PI.LONGITUDE, single.getString(Constant.PI.LONGITUDE));
				
				try {
					placeIt.put(Constant.PI.REPEATED_DAY_IN_WEEK, single.getString(Constant.PI.REPEATED_DAY_IN_WEEK));
					placeIt.put(Constant.PI.NUM_OF_WEEK_REPEAT, single.getString(Constant.PI.NUM_OF_WEEK_REPEAT));
				}catch(JSONException e) {}
					
				try {
					placeIt.put(Constant.PI.REPEATED_MINUTE, single.getString(Constant.PI.REPEATED_MINUTE));
				}catch(JSONException e) {}
			}
			else {
				// It's a category place it
				placeIt.put(Constant.PI.CATEGORY_ONE, single.getString(Constant.PI.CATEGORY_ONE));

				String two = single.optString(Constant.PI.CATEGORY_TWO);
				if( !two.isEmpty() ) {
					placeIt.put(Constant.PI.CATEGORY_TWO, two);
				}

				String three = single.optString(Constant.PI.CATEGORY_THREE);
				if( !three.isEmpty() ) {
					placeIt.put(Constant.PI.CATEGORY_THREE, three);
				}
			}

			try {
				placeIt.put(Constant.PI.DESCRIPTION, single.getString(Constant.PI.DESCRIPTION));
			} catch(JSONException e) {}
		}catch(JSONException e) {
			Log.wtf("getPlaceIt()", "Corrupt data " + e.toString());
		}
		
		return placeIt;
	}
}