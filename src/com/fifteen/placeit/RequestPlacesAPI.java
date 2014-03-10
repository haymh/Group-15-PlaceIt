package com.fifteen.placeit;

import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

//Request data from Google Places API
public class RequestPlacesAPI {
	private static final String tag = RequestPlacesAPI.class.getSimpleName();
	private static double latitude;
	private static double longitude;
	
//PLACES API ACCESSOR
	// Accesses Places API in the background to free up UI thread
	private class accessPlacesAPI extends AsyncTask<Void, Void, String> {
		
		@Override
		protected String doInBackground(Void... params) {
			HttpClient client = new DefaultHttpClient();
			
			try {
			    HttpResponse response = client.execute(requestPost());
			    return EntityUtils.toString(response.getEntity());
			} catch (Exception e) {
			    Log.wtf(tag, e.toString());
			    return null;
			}
		}

		@Override
		protected void onPostExecute(String result) {
			if(result != null)
				new JSONParser(result).parsePlacesAPI();
		}
	}

//CLASS DEFINITION
	public RequestPlacesAPI() {}
	
	public RequestPlacesAPI(LatLng location) {
		setLocation(location);
		requestPlacesAPI();
	}
	
	public RequestPlacesAPI(double latitude, double longitude) {
		setLocation(latitude, longitude);
		requestPlacesAPI();
	}

	// Set current location
	public void setLocation(LatLng location) {
		latitude = location.latitude;
		longitude = location.longitude;
	}
	
	public void setLocation(double latitude, double longitude) {
		RequestPlacesAPI.latitude = latitude;
		RequestPlacesAPI.longitude = longitude;
	}
	
	// Update location & get new JSON from Google Places API
	public void update(LatLng location) {
		float[] results = new float[3];
		Location.distanceBetween(latitude, longitude, location.latitude, location.longitude, results);
		// TODO Debug mode
		if(results[0] > Constant.L.REQUEST_DISTANCE_INTERVAL) {
			Log.wtf(tag, "Request made with" + String.valueOf(results[0]));
			setLocation(location);
			requestPlacesAPI();
		}
	}
	
	public void update(double latitude, double longitude) {
		float[] results = new float[3];
		Location.distanceBetween(RequestPlacesAPI.latitude, RequestPlacesAPI.longitude, latitude, longitude, results);
		// TODO Debug mode
		if(results[0] > Constant.L.REQUEST_DISTANCE_INTERVAL) {
			Log.wtf(tag, "Request made with " + String.valueOf(results[0]));
			setLocation(latitude, longitude);
			requestPlacesAPI();
		}
	}
	
	// Request data from the Places API
	private void requestPlacesAPI() {
		new accessPlacesAPI().execute();
	}

	// Formats the HttpPost to be requested
	private HttpPost requestPost() {
		String location = String.valueOf(latitude) + "," + String.valueOf(longitude);
		
		Uri uri = new Uri.Builder()
		.scheme("https")
		.authority("maps.googleapis.com")
		.path("maps/api/place/search/json")
		.appendQueryParameter("location", location)
		.appendQueryParameter("radius", String.valueOf(Constant.L.RADIUS))
		.appendQueryParameter("sensor", "true")
		//.appendQueryParameter("types", "food|zoo|airport")
		.appendQueryParameter("key", Constant.PAPI.BROWSER_API)
		.build();

		return new HttpPost(uri.toString());
	}
}
