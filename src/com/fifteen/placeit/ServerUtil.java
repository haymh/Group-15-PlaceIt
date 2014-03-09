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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

/**
 * Helper class used to communicate with the PlaceItserver.
 */
public final class ServerUtil {
	private static final String SERVER_URL = "http://1-dot-airy-dialect-514.appspot.com";
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
	
	// change a placeIt's status, return status code 400---fail 200---success 404---not found
	public static int login(final String username, final String password, final String regId){
		Map<String, String> params = new HashMap<String, String>();
		params.put(ACTION, LOGIN);
		params.put(USER_NAME, username);
		params.put(PASSWORD, password);
		params.put(GCM_ID_KEY, regId);
		try {
			return postExpectStatus(LOGIN_URL, params);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 404;
		}
	}
	
	//login this account with the server, allow few attempts, return status code 400---fail 200---success 404---not found
	public static int loginWithMultipleAttempt(final String username, final String password, final String regId){
		long backoff = BACKOFF_MILLI_SECONDS;
		int code = 404; //default not found
		for(int i = 0; i < MAX_ATTEMPTS; i++){
			code = login(username, password, regId);
			if(code == 409) // conflict, username exists
				return code;
			if(code != 200){ // did not succeed
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
		Map<String, String> params = new HashMap<String, String>();
		params.put(ACTION, REGISTER);
		params.put(USER_NAME, username);
		params.put(PASSWORD, password);
		params.put(GCM_ID_KEY, regId);
		try {
			return postExpectStatus(REGISTER_URL, params);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 404;
		}
	}
	
	//register this account with the server, allow few attempts
	public static int registerWithMultipleAttempt(final String username, final String password, final String regId){
		long backoff = BACKOFF_MILLI_SECONDS;
		int code = 404; //default not found
		for(int i = 0; i < MAX_ATTEMPTS; i++){
			code = register(username, password, regId);
			if(code == 409) // conflict, username exists
				return code;
			if(code != 200){ // did not succeed
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
		params.put(ACTION, PULL);
		return postExpectString(PLACE_IT_URL, params);
	}
	
	// get all place its
	public static String init() throws IOException{
		Map<String, String> params = new HashMap<String, String>();
		params.put(ACTION, INIT);
		return postExpectString(PLACE_IT_URL, params);
	}
	
	// change a placeIt's status, return status code 400---fail 200---success 404---not found
	public static int changeStatus(long id, int status){
		Map<String, String> params = new HashMap<String, String>();
		params.put(ACTION, UPDATE);
		params.put(Constant.PI.ID, "" + id);
		params.put(Constant.PI.STATUS, "" + status);
		try {
			return postExpectStatus(PLACE_IT_URL, params);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 404;
		}
	}
	
	// create a placeIt on server, return status code 400---fail 200---success 404---not found
	public static int createPlaceIt(Map<String, String> params){
		params.put(ACTION, CREATE);
		try{
			return postExpectStatus(PLACE_IT_URL, params);
		} catch(IOException e){
			e.printStackTrace();
			return 404;
		}
	}
	
	// delete a placeIt on server, return status code 400---fail 200---success 404---not found
	public static int deletePlaceIt(long id){
		Map<String, String> params = new HashMap<String, String>();
		params.put(ACTION, DELETE);
		params.put(Constant.PI.ID, "" + id);
		try{
			return postExpectStatus(PLACE_IT_URL, params);
		} catch(IOException e){
			e.printStackTrace();
			return 404;
		}
	}

	/**
	 * Issue a POST request to the server.
	 *
	 * @param endpoint POST address.
	 * @param params request parameters.
	 *
	 * @throws IOException propagated from POST.
	 */
	private static int postExpectStatus(String endpoint, Map<String, String> params)
			throws IOException {
		URL url;
		try {
			url = new URL(endpoint);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("invalid url: " + endpoint);
		}
		StringBuilder bodyBuilder = new StringBuilder();
		Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
		// constructs the POST body using the parameters
		while (iterator.hasNext()) {
			Entry<String, String> param = iterator.next();
			bodyBuilder.append(param.getKey()).append('=')
			.append(param.getValue());
			if (iterator.hasNext()) {
				bodyBuilder.append('&');
			}
		}
		String body = bodyBuilder.toString();
		byte[] bytes = body.getBytes();
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setFixedLengthStreamingMode(bytes.length);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded;charset=UTF-8");
			// post the request
			OutputStream out = conn.getOutputStream();
			out.write(bytes);
			out.close();
			// handle the response
			return conn.getResponseCode();
		} finally {
			if (conn != null) {
				conn.disconnect();			
			}
		}
	}
	
	private static String postExpectString(String endpoint, Map<String, String> params)
			throws IOException {
		URL url;
		try {
			url = new URL(endpoint);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("invalid url: " + endpoint);
		}
		StringBuilder bodyBuilder = new StringBuilder();
		Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
		// constructs the POST body using the parameters
		while (iterator.hasNext()) {
			Entry<String, String> param = iterator.next();
			bodyBuilder.append(param.getKey()).append('=')
			.append(param.getValue());
			if (iterator.hasNext()) {
				bodyBuilder.append('&');
			}
		}
		String body = bodyBuilder.toString();
		byte[] bytes = body.getBytes();
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setFixedLengthStreamingMode(bytes.length);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded;charset=UTF-8");
			// post the request
			OutputStream out = conn.getOutputStream();
			out.write(bytes);
			out.close();
			// handle the response
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuffer sb = new StringBuffer();
			String line;
			while((line = in.readLine()) != null){
				sb.append(line);
			}
			return sb.toString();
		} finally {
			if (conn != null) {
				conn.disconnect();			
			}
		}
	}
	
	
}
