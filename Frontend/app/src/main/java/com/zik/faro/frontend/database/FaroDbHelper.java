package com.zik.faro.frontend.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by gaurav on 9/2/17.
 */

public class FaroDbHelper extends SQLiteOpenHelper {
    public static final String DB_FILE_NAME = "faro-android.db";
    public static final int DB_VERSION = 1;

    public FaroDbHelper(Context context) {
        super(context, DB_FILE_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the Events table
        db.execSQL(EventORM.SQL_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle App upgrade
        // TODO: Maintain the data in the older db, by first exporting it and saving to a JSON file, then dropping the database.
        // TODO: Recreate the database as per the new version and then re import the data
        db.execSQL(EventORM.SQL_DELETE);
        onCreate(db);
    }
}
