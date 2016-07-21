package ru.anov.qzproject.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbOpenHelper extends SQLiteOpenHelper {
	
	private static final int DB_VERSION = 1;
	private static final String DB_NAME = "qz";	
	
	public static final String THEMES_TABLE_NAME = "themes";
	public static final String FAVORITES_TABLE_NAME = "favorites";
	public static final String NEW_TABLE_NAME = "new";
	public static final String MESSAGES_TABLE_NAME = "messages";
	
	public static final String ID_FIELD = "id";
	public static final String NAME_FIELD = "name";
	public static final String DESCRIPTION_FIELD = "description";
	public static final String PARENT_FIELD = "parent";
	public static final String POPULARITY_FIELD = "popularity";
	public static final String RANGE_FIELD = "range";
	public static final String LOCKED_FIELD = "locked";
	public static final String COUNTER_FIELD = "counter";
	
	public static final String MESSAGE_FIELD = "message";
	public static final String THUMBNAIL_IMG_URL_FIELD = "thumbnail_img_url";
	public static final String TYPE_FIELD = "type";
	public static final String RUSER_ID_FIELD = "ruser_id";
	public static final String TIMESTAMP_FIELD = "timestamp";
	
	
	private static final String CREATE_THEMES_TABLE = "create table " + THEMES_TABLE_NAME + " (" + ID_FIELD + " TEXT UNIQUE, " + NAME_FIELD
			+ " TEXT, " + DESCRIPTION_FIELD + " TEXT, " + PARENT_FIELD + " TEXT, " + POPULARITY_FIELD + " TEXT, " + RANGE_FIELD + " TEXT, " + LOCKED_FIELD + " INTEGER)";
	
	private static final String CREATE_FAVORITES_TABLE = "create table " + FAVORITES_TABLE_NAME + " (" + ID_FIELD + " TEXT UNIQUE, " + NAME_FIELD
			+ " TEXT, " + DESCRIPTION_FIELD + " TEXT, " + PARENT_FIELD + " TEXT, " + POPULARITY_FIELD + " TEXT, " + RANGE_FIELD + " TEXT, " + LOCKED_FIELD + " INTEGER, " + COUNTER_FIELD + " INTEGER)";
	
	private static final String CREATE_NEW_TABLE = "create table " + NEW_TABLE_NAME + " (" + ID_FIELD + " TEXT UNIQUE, " + NAME_FIELD
			+ " TEXT, " + DESCRIPTION_FIELD + " TEXT, " + PARENT_FIELD + " TEXT, " + POPULARITY_FIELD + " TEXT, " + RANGE_FIELD + " TEXT, " + LOCKED_FIELD + " INTEGER)";
	
	private static final String CREATE_MESSAGES_TABLE = "create table " + MESSAGES_TABLE_NAME + " (" + ID_FIELD + " INTEGER PRIMARY KEY AUTOINCREMENT, " + NAME_FIELD + " TEXT, " + MESSAGE_FIELD + " TEXT, " + THUMBNAIL_IMG_URL_FIELD + " TEXT, " + TYPE_FIELD + " TEXT, " + RUSER_ID_FIELD + " TEXT, " + TIMESTAMP_FIELD + " TEXT)";
	
	
	public DbOpenHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
	    db.execSQL(CREATE_THEMES_TABLE);
	    db.execSQL(CREATE_FAVORITES_TABLE);
	    db.execSQL(CREATE_NEW_TABLE);
	    db.execSQL(CREATE_MESSAGES_TABLE);
	}	

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}
}
