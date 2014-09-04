package com.stealz.db;

/**
@author - Vedang Jadhav
**/
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbUtil
{
	private static final String DB_NAME = "LOCATE_ME_DATABASE";
	private static final int DB_VERSION = 1;
	private static final String TABLE_NAME = "locateme_table";
	private static final String ROW_ID = "_id";
	private static final String ADDRESS = "address";
	private static final String REMINDER_NAME = "reminder_name";
	private static final String DATE_CREATED = "date_created";
	private static final String CURRENT_STATUS = "current_status";
	
	private static final String CREATE_TABLE_LOCATEME = 
	"create table " + TABLE_NAME + " (" + ROW_ID + " integer primary key autoincrement, " 
	+ ADDRESS + " text, "
	+ REMINDER_NAME + " text, "
	+ CURRENT_STATUS + " text, "
	+ DATE_CREATED + " text);";
	
	private final Context context;
	
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;
	
	private static class DatabaseHelper extends SQLiteOpenHelper
	{
		DatabaseHelper(Context context) 
		{super(context, DB_NAME, null, DB_VERSION);}

		@Override
		public void onCreate(SQLiteDatabase db) 
		{
			db.execSQL(CREATE_TABLE_LOCATEME);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
		{}
	}
	
	public DbUtil(Context c)
	{this.context = c;}

	public DbUtil open() throws SQLException
	{
		mDbHelper = new DatabaseHelper(context);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	public void close()
	{mDbHelper.close();}

	public long addRow(String address,String reminderName,String date_created, String current_status)
	{
		ContentValues cv = new ContentValues();
		
		cv.put(ADDRESS, address);
		cv.put(REMINDER_NAME, reminderName);
		cv.put(DATE_CREATED, date_created);
		cv.put(CURRENT_STATUS, current_status);
		
		return mDb.insert(TABLE_NAME, null, cv);
	}
	
	public int updateRow(int id,String currentStatus)
	{
		ContentValues cv = new ContentValues();
		cv.put(CURRENT_STATUS, currentStatus);
		return mDb.update(TABLE_NAME, cv, "_id = ?", new String[]{new Integer(id).toString()});
	}
	
	public Cursor fetchAllValues()
	{return mDb.query(TABLE_NAME,new String[] {ROW_ID, ADDRESS,DATE_CREATED,CURRENT_STATUS,REMINDER_NAME},null,null,null,null,null);}

	public Cursor query(String TableName,String[] columns,String whereClause,String[] whereValue)
	{return mDb.query(TableName, columns, whereClause, whereValue, null, null, null, null);}
	
	public boolean deleteTitle(String rowId) 
	{return mDb.delete(TABLE_NAME,ROW_ID + "=" + rowId, null)>0;}
	
}
