package com.fifteen.placeit;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.fifteen.placeit.PlaceIt.NumOfWeekRepeat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.*;

public class DatabaseAccessor {

	private SQLiteDatabase database;
	private MySQLiteHelper dbHelper;
	private String[] allColumns = {MySQLiteHelper.COLUMN_ID, MySQLiteHelper.COLUMN_TITLE,
			MySQLiteHelper.COLUMN_DESCRIPTION, MySQLiteHelper.COLUMN_REPEAT_BY_MIN,
			MySQLiteHelper.COLUMN_REPEATED_MIN, MySQLiteHelper.COLUMN_REPEAT_BY_WEEK,
			MySQLiteHelper.COLUMN_REPEATED_DAY_IN_WEEK, MySQLiteHelper.COLUMN_NUM_OF_WEEK_REPEAT,
			MySQLiteHelper.COLUMN_CREATE_DATE, MySQLiteHelper.COLUMN_POST_DATE,
			MySQLiteHelper.COLUMN_STATUS,
			MySQLiteHelper.COLUMN_LATITUDE, MySQLiteHelper.COLUMN_LONGITUDE
	};
	public DatabaseAccessor(Context context){
		dbHelper = new MySQLiteHelper(context);
	}
	
	public void open() throws SQLException{
		database = dbHelper.getWritableDatabase();
	}
	
	public void close(){
		dbHelper.close();
	}
	
	// a helper method to search Place-its by status, parameter and means it AND or OR conjuction, status is a array of where conditions
	private Map<Long, PlaceIt> searchPlaceItByStatus(boolean and, PlaceIt.Status[] status){
		String where = null;
		if(status != null && status.length >= 1){
			String conjunction = " or ";
			if(and)
				conjunction = " and ";
			where = MySQLiteHelper.COLUMN_STATUS + " = " + status[0].getValue();
			for(int i = 1; i < status.length; i++)
				where += conjunction + MySQLiteHelper.COLUMN_STATUS + " = " + status[i].getValue();
		}
		Cursor cursor = database.query(MySQLiteHelper.TABLE_PLACE_IT,
				allColumns, where, null, null, null, null);
		int count = cursor.getCount();
		cursor.moveToFirst();
		Map<Long, PlaceIt> map = new HashMap<Long, PlaceIt>();
		for(int i = 0; i < count; i++){
			PlaceIt pi = cursorToPlaceIt(cursor);
			map.put(pi.getId(), pi);
			cursor.moveToNext();
		}
		return map;
	}
	
	// user friendly method to get active list
	public Map<Long, PlaceIt> activePlaceIt(){
		return searchPlaceItByStatus(false, new PlaceIt.Status[] {PlaceIt.Status.ACTIVE, PlaceIt.Status.ON_MAP});
	}
	
	// user friendly method to get pull down list
	public Map<Long, PlaceIt> pulldownPlaceIt(){
		return searchPlaceItByStatus(false, new PlaceIt.Status[] {PlaceIt.Status.PULL_DOWN});
	}
	
	// user friendly method to get on map list
	public Map<Long, PlaceIt> onMapPlaceIt(){
		return searchPlaceItByStatus(false, new PlaceIt.Status[] {PlaceIt.Status.ON_MAP});
	}
	
	// user friendly method to get prepost list
	public Map<Long, PlaceIt> prepostPlaceIt(){
		return searchPlaceItByStatus(false, new PlaceIt.Status[] {PlaceIt.Status.ACTIVE});
	}
	
	// insert a new place-it into database
	public PlaceIt insertPlaceIt(String title, String description, boolean repeatByMinute,
			int repeatedMinute, boolean repeatByWeek,
			int repeatedDayInWeek, NumOfWeekRepeat numOfWeekRepeat,
			Date createDate, Date postDate, double latitude, double longitude){
		Log.v("database accessor","enter insert method");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_TITLE, title);
		values.put(MySQLiteHelper.COLUMN_DESCRIPTION, description);
		values.put(MySQLiteHelper.COLUMN_REPEAT_BY_MIN, repeatByMinute);
		values.put(MySQLiteHelper.COLUMN_REPEATED_MIN, repeatedMinute);
		values.put(MySQLiteHelper.COLUMN_REPEAT_BY_WEEK, repeatByWeek);
		values.put(MySQLiteHelper.COLUMN_REPEATED_DAY_IN_WEEK, repeatedDayInWeek);
		Log.v("database accessor","before numOfWeekRepeat.getValue()");
		values.put(MySQLiteHelper.COLUMN_NUM_OF_WEEK_REPEAT, numOfWeekRepeat.getValue());
		Log.v("database accessor","numOfWeekRepeat.getValue()");
		values.put(MySQLiteHelper.COLUMN_CREATE_DATE, dateFormat.format(createDate));
		values.put(MySQLiteHelper.COLUMN_POST_DATE, dateFormat.format(postDate));
		values.put(MySQLiteHelper.COLUMN_STATUS, PlaceIt.Status.ACTIVE.getValue());
		values.put(MySQLiteHelper.COLUMN_LATITUDE, latitude);
		values.put(MySQLiteHelper.COLUMN_LONGITUDE, longitude);
		Log.v("database accessor","ready to insert");
		long insertId = database.insert(MySQLiteHelper.TABLE_PLACE_IT, null, values);
		Cursor cursor = database.query(MySQLiteHelper.TABLE_PLACE_IT,
				allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId , null, null, null, null);
		cursor.moveToFirst();
		Log.v("database accessor","inserted");
		return cursorToPlaceIt(cursor);
	}
	
	// repost a place-it 
	public boolean repostPlaceIt(PlaceIt pi){
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_CREATE_DATE, dateFormat.format(pi.getCreateDate()));
		values.put(MySQLiteHelper.COLUMN_POST_DATE, dateFormat.format(pi.getPostDate()));
		int row = database.update(MySQLiteHelper.TABLE_PLACE_IT, values, 
				MySQLiteHelper.COLUMN_ID + " = " + pi.getId(), null);
		return row == 1;
	}
	
	// pull down a place-it
	public boolean pullDown(long id){ 
		return updatePlaceItStatus(id, PlaceIt.Status.PULL_DOWN);
	}
	
	// change a place-it status to on map
	public boolean onMap(long id){ 
		return updatePlaceItStatus(id, PlaceIt.Status.ON_MAP);
	}
	
	// delete a place-it in database
	public boolean discard(long id){
		return database.delete(MySQLiteHelper.TABLE_PLACE_IT,
				MySQLiteHelper.COLUMN_ID + " = " + id, null) > 0;
	}
	
	
	
	// check a place-it's status
	public PlaceIt.Status checkStatus(long id){
		Cursor cursor = database.query(MySQLiteHelper.TABLE_PLACE_IT,
				new String[] { MySQLiteHelper.COLUMN_STATUS }, MySQLiteHelper.COLUMN_ID + " = " + id , null, null, null, null);
		cursor.moveToFirst();
		return PlaceIt.Status.genStatus(cursor.getInt(0));
	}
	
	// find a place-it by id
	public PlaceIt findPlaceIt(long id){
		String where = MySQLiteHelper.COLUMN_ID+ " = " + id;
		Cursor cursor = database.query(MySQLiteHelper.TABLE_PLACE_IT,
				allColumns, where, null, null, null, null);
		int count = cursor.getCount();
		if(count <= 0)
			return null;
		cursor.moveToFirst();
		return cursorToPlaceIt(cursor);
	}
	
	
	// update place-it status in database
	private boolean updatePlaceItStatus(long id, PlaceIt.Status status){
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_STATUS, status.getValue());
		int row = database.update(MySQLiteHelper.TABLE_PLACE_IT, values, 
				MySQLiteHelper.COLUMN_ID + " = " + id, null);
		return row == 1;		
	}
	
	
	//helper method that turns each row into a PlaceIt object
	private PlaceIt cursorToPlaceIt(Cursor cursor){
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			return new PlaceIt(cursor.getLong(0), cursor.getString(1), cursor.getString(2),
					cursor.getInt(3) == 1, cursor.getInt(4), cursor.getInt(5) == 1, cursor.getInt(6),
					PlaceIt.NumOfWeekRepeat.genNumOfWeekRepeat(cursor.getInt(7)), 
					dateFormat.parse(cursor.getString(8)), dateFormat.parse(cursor.getString(9)),
					cursor.getDouble(11),cursor.getDouble(12),
					PlaceIt.Status.genStatus(cursor.getInt(10))
					);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
}
