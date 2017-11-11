package com.zik.faro.frontend.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.text.MessageFormat;

/**
 * Created by gaurav on 9/9/17.
 */

public class FaroDataSource {
    private String TAG = "FaroDataSource";
    private Context context;
    private SQLiteOpenHelper dbHelper;
    private SQLiteDatabase database;

    public FaroDataSource(Context context) {
        this.context = context;
        this.dbHelper = new FaroDbHelper(context);
    }

    /**
     * Open the database and establish connections to it.
     */
    public void open() {
        // Open the database
        // This will automatically call the onCreate method of dbHelper if the database has not been created previously
        this.database = dbHelper.getWritableDatabase();
    }

    /**
     * Close existing connections to the database
     */
    public void close() {
        dbHelper.close();
    }

    public void createEntry(ContentValues contentValues, String tableName) throws FaroDatasourceException {
        long insertRow = database.insertWithOnConflict(tableName, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
        if (insertRow == -1) {
            throw new FaroDatasourceException(MessageFormat.format("Failed to create entry {0} into table {1}", contentValues, tableName));
        }
    }

    public Cursor getAllEntries(String tableName, String[] columns) {
        return database.query(tableName, columns, null, null, null, null, null);
    }

    public long getNumEntries(String tableName) {
        return DatabaseUtils.queryNumEntries(database, tableName);
    }

}
