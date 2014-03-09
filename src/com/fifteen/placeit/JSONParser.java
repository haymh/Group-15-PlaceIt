package com.fifteen.placeit;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

//Parses JSON
public class JSONParser {
	private static final String tag = JSONParser.class.getSimpleName();
	
	private String string;
	
	// Places API map. <Type, Address>
	private static HashMap<String, String> placesAPIMap = new HashMap<String, String>();
	
	// Used as accessor
	public JSONParser() {}
	
	public JSONParser(String data) {
		setString(data);
	}
	
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
				
				//Log.wtf("GOT", address);
				
				// Stores address into the given type
				JSONArray types = new JSONArray(single.getString("types"));
				for(int t = 0; t < types.length(); ++t) {
					placesAPIMap.put(types.getString(t), address);
					
					//Log.wtf("", types.getString(t));
				}
			} catch (JSONException e) {
				Log.wtf(tag, "setupPlacesAPIMap()\n" + e.toString());
			}
			
			System.out.println();
		}
	}
	
	public String getAddress(String category) {
		return placesAPIMap.get(category);
	}
}