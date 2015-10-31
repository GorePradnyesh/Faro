package com.zik.faro.data;


import java.util.Date;

public class DateOffset {
    public final Date date;
    public final long offset;


    public DateOffset(Date date, long offset) {
        this.date = date;
        this.offset = offset;
    }
}
