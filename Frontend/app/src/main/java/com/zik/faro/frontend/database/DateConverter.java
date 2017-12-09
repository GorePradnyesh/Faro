package com.zik.faro.frontend.database;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by gaurav on 11/14/17.
 */

public class DateConverter {

    public static Calendar toCalendar(Long timeInMilliSecs) {
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTimeInMillis(timeInMilliSecs);

        return calendar;
    }

    public static Long toTimeInMilliSecs() {

    }
}
