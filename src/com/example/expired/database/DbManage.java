package com.example.expired.database;

import android.database.Cursor;
import android.database.sqlite.*;

import java.lang.*;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import android.provider.BaseColumns;
import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.Days;

public class DbManage {
		
		//Constant Data
		// If you change the database schema, you must increment the database version.
	public static final String TAG = "DbManage";
	public static final String KEY_ROWID = "_ID";
	public static final int COL_ROWID = 0;
	
	//Fields
	
    public static final String KEY_PRODNAME = "ProductName";
    public static final String KEY_BOUGHT = "DateBought";
    public static final String KEY_EXPDATE = "ExpirationDate";
    //public static final String KEY_DAYSTOGO = "DaysToGo";
    
    //Fields Indexes
    public static final int COL_PRODNAME = 1;
    public static final int COL_BOUGHT = 2;
    public static final int COL_EXPDATE = 3;
    //public static final int COL_DAYSTOGO = 4;
    
    public static final int DATABASE_VERSION = 6;
    public static final String DATABASE_NAME = "Expired.db";
    
    
    public static final String[] ALL_KEYS = new String[] {KEY_ROWID, KEY_PRODNAME, 
    										KEY_BOUGHT, KEY_EXPDATE};
    												
    public static final String DATABASE_TABLE = "MainTable";
    
    public static final String DATABASE_CREATE_SQL = 
    		"create table " + DATABASE_TABLE 
    		+ "(" + KEY_ROWID + " integer primary key autoincrement, " 
    		+ KEY_PRODNAME + " text not null, "
    		+ KEY_BOUGHT + " datetime not null, "
    		+ KEY_EXPDATE + " datetime not null"
    		//+ KEY_DAYSTOGO + " integer "
    		+ ");";
    
    // Context of application who uses us.
 	private final Context context;
 	
 	private DatabaseHelper DBHelperExp;
 	private SQLiteDatabase db;
    
    public DbManage(Context ctx) {
    	this.context =ctx;
    	DBHelperExp = new DatabaseHelper(context);
    }
    
 // Open the database connection.
 	public DbManage open() {
 		db = DBHelperExp.getWritableDatabase();
 		return this;
 	}
 	
 	// Close the database connection.
 	public void close() {
 		DBHelperExp.close();
 	}
 	
 	public Date ParseDate(String str) {
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");
 		Date date = null;
		try {
			date = sdf.parse(str);
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return date;
 		
 	}
 // Add a new set of values to the database.
 	public long insertRow(String prodname, String bought, String expdate) {
 		
 		Date date_bought = ParseDate(bought);
 		Date date_exp = ParseDate(expdate);
 		
		int days = Days.daysBetween(
	 				new DateTime(date_bought), 
	 				new DateTime(date_exp)).getDays();
		
 		
 		// Create row's data:
 		ContentValues initialValues = new ContentValues();
 		
 		initialValues.put(KEY_PRODNAME, prodname);
 		initialValues.put(KEY_BOUGHT, bought);
 		initialValues.put(KEY_EXPDATE, expdate);
 		//initialValues.put(KEY_DAYSTOGO, days);
 		
 		// Insert it into the database.
 		return db.insert(DATABASE_TABLE, null, initialValues);
 	}
 // Delete a row from the database, by rowId (primary key)
 	public boolean deleteRow(long rowId) {
 		String where = KEY_ROWID + "=" + rowId;
 		return db.delete(DATABASE_TABLE, where, null) != 0;
 	}
 	
 	public void deleteAll() {
 		Cursor c = getAllRows();
 		long rowId = c.getColumnIndexOrThrow(KEY_ROWID);
 		if (c.moveToFirst()) {
 			do {
 				deleteRow(c.getLong((int) rowId));				
 			} while (c.moveToNext());
 		}
 		c.close();
 	}
 	
 // Get a specific row (by rowId)
 	public Cursor getRow(long rowId) {
 		String where = KEY_ROWID + "=" + rowId;
 		Cursor c = 	db.query(true, DATABASE_TABLE, ALL_KEYS, 
 						where, null, null, null, null, null);
 		if (c != null) {
 			c.moveToFirst();
 		}
 		return c;
 	}
 // Return all data in the database.
 	public Cursor getAllRows() {
 		String where = null;
 		Cursor c = 	db.query(true, DATABASE_TABLE, ALL_KEYS, 
 							where, null, null, null, null, null);
 		if (c != null) {
 			c.moveToFirst();
 		}
 		return c;
 	}
 	
 	//Change an existing row to be equal to the new values
 	public boolean updateRow(long rowId, String prodname, 
 			String bought, String expdate) {
		String where = KEY_ROWID + "=" + rowId;
		
		ContentValues newValues = new ContentValues();
 		
 		newValues.put(KEY_PRODNAME, prodname);
 		newValues.put(KEY_BOUGHT, bought);
 		newValues.put(KEY_EXPDATE, expdate);
 		
 		
 		return db.update(DATABASE_TABLE, newValues, where, null) != 0;
 	}
    
    
    /**
	 * Private class which handles database creation and upgrading.
	 * Used to handle low-level database access.
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase _db) {
			_db.execSQL(DATABASE_CREATE_SQL);			
		}

		@Override
		public void onUpgrade(SQLiteDatabase _db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading application's database from version " + oldVersion
					+ " to " + newVersion + ", which will destroy all old data!");
			
			// Destroy old database:
			_db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
			
			// Recreate new database:
			onCreate(_db);
		}
	}
		
		

	   
	    
	    
	

}
