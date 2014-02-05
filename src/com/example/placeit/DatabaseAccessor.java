package com.example.placeit;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.example.placeit.PlaceIt.NumOfWeekRepeat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseAccessor {

	private SQLiteDatabase database;
	private MySQLiteHelper dbHelper;
	private String[] allColumns = {MySQLiteHelper.COLUMN_ID, MySQLiteHelper.COLUMN_TITLE,
			MySQLiteHelper.COLUMN_DESCRIPTION, MySQLiteHelper.COLUMN_IS_REPEATED,
			MySQLiteHelper.COLUMN_REPEATED_DAY_IN_WEEK, MySQLiteHelper.COLUMN_NUM_OF_WEEK_REPEAT,
			MySQLiteHelper.COLUMN_CREATE_DATE, MySQLiteHelper.COLUMN_POST_DATE,
			MySQLiteHelper.COLUMN_EXPIRATION, MySQLiteHelper.COLUMN_STATUS,
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
	
	public PlaceIt insertPlaceIt(String title, String description, boolean isRepeated,
			int repeatedDayInWeek, NumOfWeekRepeat numOfWeekRepeat,
			Date createDate, Date postDate, Date expiration, double latitude,
			double longitude){
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_TITLE, title);
		values.put(MySQLiteHelper.COLUMN_DESCRIPTION, description);
		values.put(MySQLiteHelper.COLUMN_IS_REPEATED, isRepeated);
		values.put(MySQLiteHelper.COLUMN_REPEATED_DAY_IN_WEEK, repeatedDayInWeek);
		values.put(MySQLiteHelper.COLUMN_NUM_OF_WEEK_REPEAT, numOfWeekRepeat.getValue());
		values.put(MySQLiteHelper.COLUMN_CREATE_DATE, dateFormat.format(createDate));
		values.put(MySQLiteHelper.COLUMN_POST_DATE, dateFormat.format(postDate));
		values.put(MySQLiteHelper.COLUMN_EXPIRATION, dateFormat.format(expiration));
		values.put(MySQLiteHelper.COLUMN_STATUS, PlaceIt.Status.ACTIVE.getValue());
		values.put(MySQLiteHelper.COLUMN_LATITUDE, latitude);
		values.put(MySQLiteHelper.COLUMN_LONGITUDE, longitude);
		long insertId = database.insert(MySQLiteHelper.TABLE_PLACE_IT, null, values);
		Cursor cursor = database.query(MySQLiteHelper.TABLE_PLACE_IT,
				allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId , null, null, null, null);
		cursor.moveToFirst();
		return cursorToPlaceIt(cursor);
	}
	
	private PlaceIt cursorToPlaceIt(Cursor cursor){
		return new PlaceIt(cursor.getLong(0), cursor.getString(1), cursor.getString(2),
				cursor.getInt(3) == 1, cursor.getInt(4), PlaceIt.NumOfWeekRepeat.genNumOfWeekRepeat(cursor.getInt(5)), 
				new Date(cursor.getString(6)), new Date(cursor.getString(7)),
				new Date(cursor.getString(8)), PlaceIt.Status.genStatus(cursor.getInt(9)),
				cursor.getDouble(10),cursor.getDouble(11));
	}
}
