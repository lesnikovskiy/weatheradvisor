package com.lesnikovski.data;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.lesnikovski.models.LocalData;
import com.lesnikovski.utils.Utils;

public class LocalDataAdapter {
	static final private String TAG = "LocalDataAdapter";
	
	static final private String DB_NAME = "weather.db";	
	static final private int DB_VERSION = 1;
	
	static final private String TABLE_NAME = "weather";
	
	static final private String ID = "id";
	static final private String OBSERVATION_TIME = "observation_time";
	static final private String TEMP_C = "tempC";
	static final private String VISIBILITY = "visibility";
	static final private String CLOUDCOVER = "cloudcover";
	static final private String HUMIDITY = "humidity";
	static final private String PRESSURE = "pressure";
	static final private String WINDSPEEDKMPH = "windspeedKmph";
	
	static final private int ID_COLUMN = 0;
	static final private int OBSERVATION_TIME_COLUMN = 1;
	static final private int TEMP_C_COLUMN = 2;
	static final private int VISIBILITY_COLUMN = 3;
	static final private int CLOUDCOVER_COLUMN = 4;
	static final private int HUMIDITY_COLUMN = 5;
	static final private int PRESSURE_COLUMN = 6;
	static final private int WINDSPEEDKMPH_COLUMN = 7;
	
	private LocalDataOpenHelper openHelper;
	private SQLiteDatabase db;
	
	public LocalDataAdapter(Context context) {
		openHelper = new LocalDataOpenHelper(context, DB_NAME, null, DB_VERSION);
	}
	
	public void open() throws SQLiteException {
		try {
			db = openHelper.getWritableDatabase();
		} catch (SQLiteException e) {
			Log.e(TAG, e.getMessage());
			
			db = openHelper.getReadableDatabase();
		}
	}
	
	public void close() {
		db.close();
		openHelper.close();
	}
	
	public List<LocalData> getLocalData() {
		List<LocalData> data = new ArrayList<LocalData>();
		
		Cursor c = db.query(TABLE_NAME, 
				new String[] {ID, OBSERVATION_TIME, TEMP_C, VISIBILITY, CLOUDCOVER, HUMIDITY, PRESSURE, WINDSPEEDKMPH}, 
				null, null, null, null, null);
		
		if (c.moveToFirst() && c.getCount() > 0) {
			do {
				LocalData d = new LocalData();
				d.setId(c.getLong(ID_COLUMN));
				d.setObservationTime(c.getString(OBSERVATION_TIME_COLUMN));
				d.setTempC(c.getInt(TEMP_C_COLUMN));
				d.setVisibility(c.getInt(VISIBILITY_COLUMN));
				d.setCloudcover(c.getInt(CLOUDCOVER_COLUMN));
				d.setHumidity(c.getInt(HUMIDITY_COLUMN));
				d.setPressure(c.getInt(PRESSURE_COLUMN));
				d.setWindspeedKmph(c.getInt(WINDSPEEDKMPH_COLUMN));
				
				data.add(d);
			} while (c.moveToNext());
		}
		
		return data;
	}
	
	public LocalData getLastData() {
		LocalData data = new LocalData();
		
		String selectQuery = String.format("SELECT * FROM %s WHERE id = (SELECT MAX(id) FROM %s)", TABLE_NAME, TABLE_NAME);
		
		Cursor c = db.rawQuery(selectQuery, null);
		if (c.moveToFirst() && c.getCount() > 0) {			
			data.setId(c.getLong(ID_COLUMN));
			data.setObservationTime(c.getString(OBSERVATION_TIME_COLUMN));
			data.setTempC(c.getInt(TEMP_C_COLUMN));
			data.setVisibility(c.getInt(VISIBILITY_COLUMN));
			data.setCloudcover(c.getInt(CLOUDCOVER_COLUMN));
			data.setHumidity(c.getInt(HUMIDITY_COLUMN));
			data.setPressure(c.getInt(PRESSURE_COLUMN));
			data.setWindspeedKmph(c.getInt(WINDSPEEDKMPH_COLUMN));
		} else {
			data = null;
		}
		
		return data;
	}
	
	public LocalData getLastByHoursData(int hours) {		
		String condition = Utils.getDateString(hours);
		
		return getLastRecordByDate(condition);
	}
	
	public LocalData getLastByMinutesData(int minutes) {		
		String condition = Utils.getDateStringByMinutes(minutes);		
		
		return getLastRecordByDate(condition);
	}
	
	private LocalData getLastRecordByDate(String condition) {
		LocalData data = new LocalData();
		
		String selectQuery = String.format("SELECT * FROM %s WHERE id = (SELECT MAX(id) FROM %s WHERE datetime(%s) < datetime('%s'))", 
				TABLE_NAME, TABLE_NAME,  OBSERVATION_TIME, condition);
		
		Cursor cursor = db.rawQuery(selectQuery, null);
		if (cursor.moveToFirst() && cursor.getCount() > 0) {			
			data.setId(cursor.getLong(ID_COLUMN));
			data.setObservationTime(cursor.getString(OBSERVATION_TIME_COLUMN));
			data.setTempC(cursor.getInt(TEMP_C_COLUMN));
			data.setVisibility(cursor.getInt(VISIBILITY_COLUMN));
			data.setCloudcover(cursor.getInt(CLOUDCOVER_COLUMN));
			data.setHumidity(cursor.getInt(HUMIDITY_COLUMN));
			data.setPressure(cursor.getInt(PRESSURE_COLUMN));
			data.setWindspeedKmph(cursor.getInt(WINDSPEEDKMPH_COLUMN));
		} else {
			data = null;
		}
		
		return data;
	}
	
	public boolean insertData(LocalData data) {
		ContentValues values = new ContentValues();
		values.put(OBSERVATION_TIME, data.getObservationTime());
		values.put(TEMP_C, data.getTempC());
		values.put(VISIBILITY, data.getVisibility());
		values.put(CLOUDCOVER, data.getCloudcover());
		values.put(HUMIDITY, data.getHumidity());
		values.put(PRESSURE, data.getPressure());
		values.put(WINDSPEEDKMPH, data.getWindspeedKmph());
		
		return db.insert(TABLE_NAME, null, values) > 0;
	}
	
	private class LocalDataOpenHelper extends SQLiteOpenHelper {
		static final private String TAG = "LocalDataOpenHelper";
		static final private String CREATE_DB = "CREATE TABLE weather (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, observation_time text NOT NULL," + 
												" tempC INT NOT NULL, visibility INT NOT NULL, cloudcover INT NOT NULL, humidity int NOT NULL, " + 
												"pressure INT NOT NULL, windspeedKmph INT NULL);";
		static final private String MIGRATE_DB = "DROP TABLE IF EXISTS weather;";

		public LocalDataOpenHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_DB);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.d(TAG, String.format("Database migrated from version %s to version %s", oldVersion, newVersion));
			db.execSQL(MIGRATE_DB);
			onCreate(db);
		}		
	}
}
