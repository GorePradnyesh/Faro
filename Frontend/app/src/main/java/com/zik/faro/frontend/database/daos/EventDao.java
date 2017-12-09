package com.zik.faro.frontend.database.daos;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.zik.faro.frontend.database.entities.Event;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

/**
 * Created by gaurav on 11/11/17.
 */

@Dao
public interface EventDao {
    @Query("select * from event")
    List<Event> loadAllEvents();

    @Insert(onConflict = REPLACE)
    void insertEvent(Event event);

    @Insert(onConflict = REPLACE)
    void insertEvents(List<Event> events);
}
