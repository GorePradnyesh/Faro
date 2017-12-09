package com.zik.faro.frontend.database;

import android.arch.persistence.db.SupportSQLiteOpenHelper;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.DatabaseConfiguration;
import android.arch.persistence.room.InvalidationTracker;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.zik.faro.frontend.database.daos.EventDao;
import com.zik.faro.frontend.database.entities.Event;

/**
 * Created by gaurav on 11/11/17.
 */
@Database(entities = {Event.class}, version = 1)
public abstract class FaroRoomDatabase extends RoomDatabase {
    private static FaroRoomDatabase instance;
    private static final String DB_FILE_NAME = "faro-android.db";

    public abstract EventDao eventDao();

    public static FaroRoomDatabase getDatabase(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context, FaroRoomDatabase.class, DB_FILE_NAME)
                    .build();
        }

        return instance;
    }

    public static void destroyInstance() {
        instance = null;
    }
}
