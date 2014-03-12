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

	private String string;

	// Places API map. <Type, Address>
	private static HashMap<String, String> placesAPIMap = new HashMap<String, String>();

	// Place It info map. List<Places Its>
	// Map<field, content>
	private List<Map<String, String>> placeItInfoList = new ArrayList<Map<String, String>>();

	// Place It id and status map. Map<id, Status>
	private Map<Long, Integer> placeItIdStatusMap = new HashMap<Long, Integer>();

	// Places It id list List<id>
	private List<Long> placeItIdList = new ArrayList<Long>();

	private Long time;

//CLASS DEFINITION
	// Used as accessor
	public JSONParser() {}

	public JSONParser(String data) {
		setString(data);
	}

	// Set request string
	public void setString(String data) {
		string = data;
	}

//PLACES API PARSE & SUPPORT
	// Parses JSON by Places API format
	public void parsePlacesAPI() {
		placesAPIMap.clear();

		try {
			JSONObject object = new JSONObject(string);
			JSONArray array = new JSONArray(object.getString("results"));
			setupPlacesAPIMap(array);
		} catch (Exception e) {
			Log.wtf(tag, "parsePlacesAPI()\n" + e.toString());
		}
	}

	public void parsePlacesAPI(String data) {
		setString(data);
		parsePlacesAPI();
	}

	// Set ups places access map
	private void setupPlacesAPIMap(JSONArray array) {
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

	public String getAddress(String category) {
		return placesAPIMap.get(category);
	}

// PLACES IT SERVER PARSE + SUPPORT
	// Parse Place It server 
	public void parsePlaceItServer() {
		try {
			JSONObject object = new JSONObject(new JSONObject(string).getString("operation"));

			Log.wtf("O J string", new JSONObject(string).getString("operation"));

			try {
				time = new JSONObject(object.getString("time")).getLong("time");
			} catch(Exception e) {
				time = Long.valueOf(-1);
			}

			setupPlaceItInfoList(object.optString("create"));
			setupPlaceItIdStatusMap(object.optString("change"));
			setupPlaceItIdList(object.optString("delete"));

		} catch (Exception e) {
			Log.wtf("parsePlaceItServer()", e.toString());
		}
	}

	// Parses place it list
	private void setupPlaceItInfoList(String data) {		
		JSONArray array;

		try {
			array = new JSONArray(new JSONObject(data).getString("placeit"));
		}catch(JSONException e) {
			Log.wtf("setupPlaceItInfoList()", "Bad O string " + e.toString());
			return;
		}

		for( int i = 0; i < array.length(); ++i ) {
			try {
				JSONObject single = array.getJSONObject(i);

				Map<String, String> placeIt = new HashMap<String, String>();

				placeIt.put(Constant.PI.ID, single.getString(Constant.PI.ID));
				placeIt.put(Constant.PI.CREATE_DATE, single.getString(Constant.PI.CREATE_DATE));
				placeIt.put(Constant.PI.POST_DATE, single.getString(Constant.PI.POST_DATE));
				placeIt.put(Constant.PI.TITLE, single.getString(Constant.PI.TITLE));

				String location = single.optString(Constant.PI.LATITUDE);
				if( !location.isEmpty() ) {
					// It's a location place it
					placeIt.put(Constant.PI.LATITUDE, location);
					placeIt.put(Constant.PI.LONGITUDE, single.getString(Constant.PI.LONGITUDE));
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

				placeItInfoList.add(placeIt);
			}catch(JSONException e) {
				Log.wtf("setupPlaceItInfoList()", "Corrupt data " + e.toString());
			}
		}
	}

	// Parses id + status map
	private void setupPlaceItIdStatusMap(String data) {
		try {
			JSONArray array = new JSONArray(new JSONObject(data).getString("placeit"));

			for( int i = 0; i < array.length(); ++i ) {
				JSONObject single = array.getJSONObject(i);
				placeItIdStatusMap.put(single.getLong(Constant.PI.ID), single.getInt("status"));
			}
		} catch(Exception e) {
			Log.wtf("setupPlaceItIdStatusMap() ", e.toString());
		}
	}

	// Parses id list
	private void setupPlaceItIdList(String data) {
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
	public List<Map<String, String>> getPlaceItInfoList() {
		return placeItInfoList;
	}

	// Pass place it id and status
	public Map<Long, Integer> getPlaceItIdStatusMap() {
		return placeItIdStatusMap;
	}

	// Pass place it id lists
	public List<Long> getPlaceItIdList() {
		return placeItIdList;
	}
	
	// Pass back time
	public Long getTime() {
		return time;
	}
}







