package com.fifteen.placeit;

import com.google.android.gcm.GCMRegistrar;

import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

/**
 * Helper class used to communicate with the PlaceItserver.
 */
public final class ServerUtil {
	//private static final String SERVER_URL = "http://1-dot-airy-dialect-514.appspot.com";
	private static final String SERVER_URL = "http://192.168.1.112:8888";
	private static final int MAX_ATTEMPTS = 5;
	private static final int BACKOFF_MILLI_SECONDS = 2000;
	private static final Random random = new Random();

	public static final String LOGIN_URL = SERVER_URL + "/login";
	public static final String REGISTER_URL = SERVER_URL + "/register";
	public static final String PLACE_IT_URL = SERVER_URL + "/placeit";

	public static final String USER_NAME = "username";
	public static final String PASSWORD = "password";
	public static final String ACTION = "action";
	public static final String LOGIN = "login";
	public static final String REGISTER = "register";
	public static final String CREATE = "create";
	public static final String UPDATE = "update";
	public static final String DELETE = "delete";
	public static final String INIT = "init";
	public static final String PULL = "pull";
	public static final String LAST_UPDATE = "lastUpdate";
	public static final String GCM_ID_KEY = "RegId";
	
	private static HttpClient client = new DefaultHttpClient();

	public static int OK = 200;
	public static int CONFLICT = 409;
	public static int FAIL = 400;
	public static int NOT_FOUND = 404;
	
	public static void startNewSession(){
		client = new DefaultHttpClient();
	}

	// change a placeIt's status, return status code 400---fail 200---success 404---not found
	public static int login(final String username, final String password, final String regId){
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair(ACTION,LOGIN));
		list.add(new BasicNameValuePair(USER_NAME, username));
		list.add(new BasicNameValuePair(PASSWORD, password));
		list.add(new BasicNameValuePair(GCM_ID_KEY, regId));
		return postExpectStatus(LOGIN_URL, list);
	}

	//login this account with the server, allow few attempts, return status code 400---fail 200---success 404---not found
	public static int loginWithMultipleAttempt(final String username, final String password, final String regId){
		long backoff = BACKOFF_MILLI_SECONDS;
		int code = NOT_FOUND; //default not found
		for(int i = 0; i < MAX_ATTEMPTS; i++){
			code = login(username, password, regId);
			if(code == CONFLICT) // conflict, username exists
				return code;
			if(code != OK){ // did not succeed
				try {
					Thread.sleep(backoff);
				} catch (InterruptedException e1) {
					Thread.currentThread().interrupt();
					return code;
				}
			}else
				return code;
			backoff *= 2;
		}
		return code;
	}


	/**
	 * Register this account/device pair within the server.
	 *
	 * @return whether the registration succeeded or not.
	 */
	public static int register(final String username, final String password, final String regId) {
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair(ACTION,REGISTER));
		list.add(new BasicNameValuePair(USER_NAME, username));
		list.add(new BasicNameValuePair(PASSWORD, password));
		list.add(new BasicNameValuePair(GCM_ID_KEY, regId));
		return postExpectStatus(REGISTER_URL, list);
	}

	//register this account with the server, allow few attempts
	public static int registerWithMultipleAttempt(final String username, final String password, final String regId){
		long backoff = BACKOFF_MILLI_SECONDS;
		int code = NOT_FOUND; //default not found
		for(int i = 0; i < MAX_ATTEMPTS; i++){
			code = register(username, password, regId);
			if(code == CONFLICT) // conflict, username exists
				return code;
			if(code != OK){ // did not succeed
				try {
					Thread.sleep(backoff);
				} catch (InterruptedException e1) {
					Thread.currentThread().interrupt();
					return code;
				}
			}else
				return code;
			backoff *= 2;
		}
		return code;
	}

	// synchronize data with server
	public static String pull(Map<String, String> params) throws IOException{
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair(ACTION,PULL));
		Set<Entry<String, String>> keyValue = params.entrySet();
		for(Entry<String, String> e:keyValue){
			list.add(new BasicNameValuePair(e.getKey(),e.getValue()));
		}
		return postExpectString(PLACE_IT_URL, list);
	}

	// get all place its
	public static String init() throws IOException{
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair(ACTION,INIT));
		return postExpectString(PLACE_IT_URL, list);
	}

	// change a placeIt's status, return status code 400---fail 200---success 404---not found
	public static int changeStatus(long id, AbstractPlaceIt.Status status){
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair(ACTION,UPDATE));
		list.add(new BasicNameValuePair(Constant.PI.ID, "" + id));
		list.add(new BasicNameValuePair(Constant.PI.STATUS, "" + status.getValue()));
		return postExpectStatus(PLACE_IT_URL, list);
	}

	// create a placeIt on server, return status code 400---fail 200---success 404---not found
	public static int createPlaceIt(Map<String, String> params){
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair(ACTION,CREATE));
		Set<Entry<String, String>> keyValue = params.entrySet();
		for(Entry<String, String> e:keyValue){
			list.add(new BasicNameValuePair(e.getKey(),e.getValue()));
		}
		return postExpectStatus(PLACE_IT_URL, list);
		
	}

	// delete a placeIt on server, return status code 400---fail 200---success 404---not found
	public static int deletePlaceIt(long id){
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(new BasicNameValuePair(ACTION,DELETE));
		list.add(new BasicNameValuePair(Constant.PI.ID, "" + id));
		return postExpectStatus(PLACE_IT_URL, list);
	}

	
	private static int postExpectStatus(String url, List<NameValuePair> nameValuePairs){
		for(NameValuePair a:nameValuePairs){
			Log.wtf(a.getName(), a.getValue());
		}
		HttpPost post = new HttpPost(url);

		try {
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = client.execute(post);
			return response.getStatusLine().getStatusCode();
		}catch(IOException e){
			e.printStackTrace();
			return NOT_FOUND;
		}
	}

	private static String postExpectString(String url, List<NameValuePair> nameValuePairs)
			throws IOException {
		HttpPost post = new HttpPost(url);

		try {
			StringBuffer sb = new StringBuffer();
			String line = null;
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = client.execute(post);
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			while((line = rd.readLine()) != null){
				sb.append(line);
			}
			return sb.toString();
		}catch(IOException e){
			e.printStackTrace();
			return null;
		}
	}


}
