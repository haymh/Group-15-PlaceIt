package com.example.placeit;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper{

	// table entries
	public static final String TABLE_PLACE_IT = "placeIt";
	public static final String COLUMN_ID = "id";
	public static final String COLUMN_TITLE = "title";
	public static final String COLUMN_DESCRIPTION = "description";
	public static final String COLUMN_REPEAT_BY_WEEK = "repeatByWeek";
	public static final String COLUMN_REPEAT_BY_MIN = "repeatByMinute";
	public static final String COLUMN_REPEATED_MIN = "repeatedMinute";
	public static final String COLUMN_REPEATED_DAY_IN_WEEK = "repeatedDayInWeek";
	public static final String COLUMN_NUM_OF_WEEK_REPEAT = "numOfWeekRepeat";
	public static final String COLUMN_CREATE_DATE = "createDate";
	public static final String COLUMN_POST_DATE = "postDate";
	public static final String COLUMN_STATUS = "status";
	public static final String COLUMN_LATITUDE = "latitude";
	public static final String COLUMN_LONGITUDE = "longitude";
	
	// database name
	private static final String DATABASE_NAME = "placeIts";
	private static final int DATABASE_VERSION = 1;
	
	// create table SQL
	private static final String DATABASE_CREATE = "create table "
			+ TABLE_PLACE_IT + "(" + COLUMN_ID
			+ " integer primary key autoincrement, "
			+ COLUMN_TITLE + " text not null, "
			+ COLUMN_DESCRIPTION + " text, "
			+ COLUMN_REPEAT_BY_MIN + " boolean not null, "
			+ COLUMN_REPEATED_MIN + " integer, "
			+ COLUMN_REPEAT_BY_WEEK + " boolean not null, "
			+ COLUMN_REPEATED_DAY_IN_WEEK + " integer, "
			+ COLUMN_NUM_OF_WEEK_REPEAT + " integer, "
			+ COLUMN_CREATE_DATE + " datetime not null, "
			+ COLUMN_POST_DATE + " datetime, "
			+ COLUMN_STATUS + " integer not null, "
			+ COLUMN_LATITUDE + " double not null, "
			+ COLUMN_LONGITUDE + " double not null);";
	public MySQLiteHelper(Context context){
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	public MySQLiteHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL(DATABASE_CREATE);
	}

	// method to handle upgrade
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		Log.w(MySQLiteHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
				+ newVersion + ", which will destroy all old data");
		db.execSQL("drop table if exists " + TABLE_PLACE_IT);
		onCreate(db);
	}
	
	
}
