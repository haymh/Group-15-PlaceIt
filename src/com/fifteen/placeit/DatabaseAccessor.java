package com.fifteen.placeit;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;



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
			MySQLiteHelper.COLUMN_DESCRIPTION, MySQLiteHelper.COLUMN_REPEATED_DAY_IN_WEEK,
			MySQLiteHelper.COLUMN_REPEATED_MIN, MySQLiteHelper.COLUMN_NUM_OF_WEEK_REPEAT,
			MySQLiteHelper.COLUMN_CREATE_DATE, MySQLiteHelper.COLUMN_POST_DATE,	
			MySQLiteHelper.COLUMN_LATITUDE, MySQLiteHelper.COLUMN_LONGITUDE,
			MySQLiteHelper.COLUMN_STATUS, MySQLiteHelper.COLUMN_CATEGORY_ONE,
			MySQLiteHelper.COLUMN_CATEGORY_TWO, MySQLiteHelper.COLUMN_CATEGORY_THREE
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
	private Map<Long, AbstractPlaceIt> searchPlaceItByStatus(boolean and, AbstractPlaceIt.Status[] status){
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
		Map<Long, AbstractPlaceIt> map = new HashMap<Long, AbstractPlaceIt>();
		for(int i = 0; i < count; i++){
			AbstractPlaceIt pi = cursorToPlaceIt(cursor);
			map.put(pi.getId(), pi);
			cursor.moveToNext();
		}
		return map;
	}
	
	// user friendly method to get active list
	public Map<Long, AbstractPlaceIt> activePlaceIt(){
		return searchPlaceItByStatus(false, new AbstractPlaceIt.Status[] {AbstractPlaceIt.Status.ACTIVE, AbstractPlaceIt.Status.ON_MAP});
	}
	
	// user friendly method to get pull down list
	public Map<Long, AbstractPlaceIt> pulldownPlaceIt(){
		return searchPlaceItByStatus(false, new AbstractPlaceIt.Status[] {AbstractPlaceIt.Status.PULL_DOWN});
	}
	
	// user friendly method to get on map list
	public Map<Long, AbstractPlaceIt> onMapPlaceIt(){
		return searchPlaceItByStatus(false, new AbstractPlaceIt.Status[] {AbstractPlaceIt.Status.ON_MAP});
	}
	
	// user friendly method to get prepost list
	public Map<Long, AbstractPlaceIt> prepostPlaceIt(){
		return searchPlaceItByStatus(false, new AbstractPlaceIt.Status[] {AbstractPlaceIt.Status.ACTIVE});
	}
	
	// insert a new place-it into database
	public AbstractPlaceIt insertPlaceIt(String title, String description, int repeatedDayInWeek, int repeatedMinute, 
			WeeklySchedule.NumOfWeekRepeat numOfWeekRepeat, Date createDate, Date postDate, double latitude,
			double longitude, AbstractPlaceIt.Status status, String[] categories){
		Log.v("database accessor","enter insert method");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_TITLE, title);
		values.put(MySQLiteHelper.COLUMN_DESCRIPTION, description);
		values.put(MySQLiteHelper.COLUMN_REPEATED_MIN, repeatedMinute);
		values.put(MySQLiteHelper.COLUMN_REPEATED_DAY_IN_WEEK, repeatedDayInWeek);
		Log.v("database accessor","before numOfWeekRepeat.getValue()");
		values.put(MySQLiteHelper.COLUMN_NUM_OF_WEEK_REPEAT, numOfWeekRepeat.getValue());
		Log.v("database accessor","numOfWeekRepeat.getValue()");
		values.put(MySQLiteHelper.COLUMN_CREATE_DATE, dateFormat.format(createDate));
		values.put(MySQLiteHelper.COLUMN_POST_DATE, dateFormat.format(postDate));
		values.put(MySQLiteHelper.COLUMN_STATUS, status.getValue());
		values.put(MySQLiteHelper.COLUMN_LATITUDE, latitude);
		values.put(MySQLiteHelper.COLUMN_LONGITUDE, longitude);
		if(categories != null)
		{
			switch(categories.length){
			case 3:
				values.put(MySQLiteHelper.COLUMN_CATEGORY_THREE, categories[2]);
			case 2:
				values.put(MySQLiteHelper.COLUMN_CATEGORY_TWO, categories[1]);
			case 1:
				values.put(MySQLiteHelper.COLUMN_CATEGORY_ONE, categories[0]);
			default:
			}
		}
		Log.v("database accessor","ready to insert");
		long insertId = database.insert(MySQLiteHelper.TABLE_PLACE_IT, null, values);
		Cursor cursor = database.query(MySQLiteHelper.TABLE_PLACE_IT,
				allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId , null, null, null, null);
		cursor.moveToFirst();
		Log.v("database accessor","inserted");
		return cursorToPlaceIt(cursor);
	}
	
	public boolean insertPlaceIt(long id, String title, String description, int repeatedDayInWeek, int repeatedMinute, 
			WeeklySchedule.NumOfWeekRepeat numOfWeekRepeat, String createDate, String postDate, double latitude,
			double longitude, AbstractPlaceIt.Status status, String[] categories){
		Log.v("database accessor","enter insert method");
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_ID, id);
		values.put(MySQLiteHelper.COLUMN_TITLE, title);
		values.put(MySQLiteHelper.COLUMN_DESCRIPTION, description);
		values.put(MySQLiteHelper.COLUMN_REPEATED_MIN, repeatedMinute);
		values.put(MySQLiteHelper.COLUMN_REPEATED_DAY_IN_WEEK, repeatedDayInWeek);
		Log.v("database accessor","before numOfWeekRepeat.getValue()");
		values.put(MySQLiteHelper.COLUMN_NUM_OF_WEEK_REPEAT, numOfWeekRepeat.getValue());
		Log.v("database accessor","numOfWeekRepeat.getValue()");
		values.put(MySQLiteHelper.COLUMN_CREATE_DATE, createDate);
		values.put(MySQLiteHelper.COLUMN_POST_DATE, postDate);
		values.put(MySQLiteHelper.COLUMN_STATUS, status.getValue());
		values.put(MySQLiteHelper.COLUMN_LATITUDE, latitude);
		values.put(MySQLiteHelper.COLUMN_LONGITUDE, longitude);
		if(categories != null)
		{
			switch(categories.length){
			case 3:
				values.put(MySQLiteHelper.COLUMN_CATEGORY_THREE, categories[2]);
			case 2:
				values.put(MySQLiteHelper.COLUMN_CATEGORY_TWO, categories[1]);
			case 1:
				values.put(MySQLiteHelper.COLUMN_CATEGORY_ONE, categories[0]);
			default:
			}
		}
		Log.v("database accessor","ready to insert");
		try{
			database.insert(MySQLiteHelper.TABLE_PLACE_IT, null, values);
			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		
	}
	
	// repost a place-it 
	public boolean repostPlaceIt(AbstractPlaceIt pi){
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_CREATE_DATE, dateFormat.format(pi.schedule.getCreateDate()));
		values.put(MySQLiteHelper.COLUMN_POST_DATE, dateFormat.format(pi.schedule.getPostDate()));
		int row = database.update(MySQLiteHelper.TABLE_PLACE_IT, values, 
				MySQLiteHelper.COLUMN_ID + " = " + pi.getId(), null);
		return row == 1;
	}
	
	// pull down a place-it
	public boolean pullDown(long id){ 
		return updatePlaceItStatus(id, AbstractPlaceIt.Status.PULL_DOWN);
	}
	
	// change a place-it status to on map
	public boolean onMap(long id){ 
		return updatePlaceItStatus(id, AbstractPlaceIt.Status.ON_MAP);
	}
	
	// change a place-it status to active
	public boolean active(long id){
		return updatePlaceItStatus(id, AbstractPlaceIt.Status.ACTIVE);
	}
	
	// delete a place-it in database
	public boolean discard(long id){
		return database.delete(MySQLiteHelper.TABLE_PLACE_IT,
				MySQLiteHelper.COLUMN_ID + " = " + id, null) > 0;
	}
	
	
	
	// check a place-it's status
	public AbstractPlaceIt.Status checkStatus(long id){
		Cursor cursor = database.query(MySQLiteHelper.TABLE_PLACE_IT,
				new String[] { MySQLiteHelper.COLUMN_STATUS }, MySQLiteHelper.COLUMN_ID + " = " + id , null, null, null, null);
		cursor.moveToFirst();
		return AbstractPlaceIt.Status.genStatus(cursor.getInt(0));
	}
	
	// find a place-it by id
	public AbstractPlaceIt findPlaceIt(long id){
		String where = MySQLiteHelper.COLUMN_ID+ " = " + id;
		Cursor cursor = database.query(MySQLiteHelper.TABLE_PLACE_IT,
				allColumns, where, null, null, null, null);
		int count = cursor.getCount();
		if(count <= 0)
			return null;
		cursor.moveToFirst();
		return cursorToPlaceIt(cursor);
	}
	
	// drop the placeIt table
	public void dropTable(){
		database.execSQL("DROP TABLE IF EXISTS " + MySQLiteHelper.TABLE_PLACE_IT);
	}
	
	// create the placeIt table
	public void createTable(){
		Cursor cursor = database.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '" + MySQLiteHelper.TABLE_PLACE_IT + "'", null);
		if(cursor == null || cursor.getCount() == 0) {
			database.execSQL(MySQLiteHelper.DATABASE_CREATE);
		}
		
	}
	
	
	// update place-it status in database
	public boolean updatePlaceItStatus(long id, AbstractPlaceIt.Status status){
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_STATUS, status.getValue());
		int row = database.update(MySQLiteHelper.TABLE_PLACE_IT, values, 
				MySQLiteHelper.COLUMN_ID + " = " + id, null);
		return row == 1;		
	}
	
	
	//helper method that turns each row into a PlaceIt object
	private AbstractPlaceIt cursorToPlaceIt(Cursor cursor){
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			long id = cursor.getLong(0);
			String title = cursor.getString(1);
			String description = cursor.getString(2);
			int repeatedDayInWeek = cursor.getInt(3);
			int repeatedMinute = cursor.getInt(4);
			WeeklySchedule.NumOfWeekRepeat numOfWeekRepeat = WeeklySchedule.NumOfWeekRepeat.genNumOfWeekRepeat(cursor.getInt(5));
			Date createDate = dateFormat.parse(cursor.getString(6));
			Date postDate = dateFormat.parse(cursor.getString(7));
			double latitude = cursor.getDouble(8);
			double longitude = cursor.getDouble(9);
			AbstractPlaceIt.Status status = AbstractPlaceIt.Status.genStatus(cursor.getInt(10));
			String categoryOne = cursor.getString(11);
			String categoryTwo = cursor.getString(12);
			String categoryThree = cursor.getString(13);
			String[] categories;
			if(categoryThree != null && !categoryThree.equals("")){
				categories = new String[3];
				categories[0] = categoryOne;
				categories[1] = categoryTwo;
				categories[2] = categoryThree;
			}else if(categoryTwo != null && !categoryTwo.equals("")){
				categories = new String[2];
				categories[0] = categoryOne;
				categories[1] = categoryTwo;
			}else if(categoryOne != null && !categoryOne.equals("")){
				categories = new String[1];
				categories[0] = categoryOne;
			}else
				categories = null;
			
			return PlaceItFactory.createPlaceIt(id, title, description, repeatedDayInWeek, repeatedMinute,
					numOfWeekRepeat, createDate, postDate, latitude, longitude, status, categories);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	
}
