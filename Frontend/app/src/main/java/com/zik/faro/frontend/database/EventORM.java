package com.zik.faro.frontend.database;

import android.content.ContentValues;

import com.google.gson.Gson;
import com.zik.faro.data.Event;
import com.zik.faro.data.EventInviteStatusWrapper;

/**
 * Created by gaurav on 9/2/17.
 */

public class EventORM {
    public static final String TABLE_ITEMS = "events";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "eventName";
    public static final String COLUMN_START_DATE = "startDate";
    public static final String COLUMN_END_DATE = "endDate";
    public static final String COLUMN_EVENT_DESCRIPTION = "eventDescription";
    public static final String COLUMN_CONTROL_FLAG = "controlFlag";
    public static final String COLUMN_INVITE_STATUS = "inviteStatus";
    public static final String COLUMN_POSITION = "sortPosition";

    public static final int NUM_COLUMNS = 7;

    public static final String SQL_CREATE =
            "CREATE TABLE " + TABLE_ITEMS + "(" +
                    COLUMN_ID + " TEXT PRIMARY KEY," +
                    COLUMN_NAME + " TEXT," +
                    COLUMN_START_DATE + " TEXT,"  +
                    COLUMN_END_DATE + " TEXT,"  +
                    COLUMN_EVENT_DESCRIPTION + " TEXT," +
                    COLUMN_CONTROL_FLAG + " TEXT," +
                    COLUMN_INVITE_STATUS + " TEXT, " +
                    COLUMN_POSITION + " INTEGER" + ");";

    public static final String SQL_DELETE =
            "DROP TABLE " + TABLE_ITEMS;

    public static final String[] ALL_COLUMNS = {COLUMN_ID, COLUMN_NAME, COLUMN_START_DATE, COLUMN_END_DATE, COLUMN_EVENT_DESCRIPTION,
            COLUMN_CONTROL_FLAG, COLUMN_INVITE_STATUS};

    public static ContentValues toContentValues(EventInviteStatusWrapper eventInviteStatusWrapper) {
        Event event = eventInviteStatusWrapper.getEvent();
        ContentValues contentValues = new ContentValues(NUM_COLUMNS);

        contentValues.put(COLUMN_ID, event.getId());
        contentValues.put(COLUMN_NAME, event.getEventName());
        // TODO : Fix this later. Storing start and end date as JSON for now
        Gson gson = new Gson();
        contentValues.put(COLUMN_START_DATE, gson.toJson(event.getStartDate()));
        contentValues.put(COLUMN_END_DATE, gson.toJson(event.getEndDate()));
        contentValues.put(COLUMN_EVENT_DESCRIPTION, event.getEventDescription());
        contentValues.put(COLUMN_CONTROL_FLAG, event.getControlFlag());
        contentValues.put(COLUMN_INVITE_STATUS, eventInviteStatusWrapper.getInviteStatus().toString());
        contentValues.put(COLUMN_POSITION, 0);

        return contentValues;
    }
}
