package com.example.nakulshah.listviewyoutube;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by nakulshah on 1/8/15.
 */
public class Event implements Parcelable{
    private String eventId;
    private String eventName;
    private int status;
    private boolean controlFlag;
    private int img_resource;


    //TODO: Change Server side code from Date Offset to Calendat
    private Calendar startDateCalendar;
    private Calendar endDateCalendar;
    private DateFormat dateFormat = DateFormat.getDateInstance();
    private String timeFormat = "hh:mm a";
    private SimpleDateFormat stf = new SimpleDateFormat(timeFormat);
    private String startDateStr = null;
    private String startTimeStr = null;
    private String endDateStr = null;
    private String endTimeStr = null;

    public Event(String eventName,
                 int status,
                 Calendar startDateCalendar,
                 Calendar endDateCalendar){
        setEventName(eventName);
        setStatus(status);
        switch (status){
            case 0:
                setImg_resource(R.drawable.green);
                break;
            case 1:
                setImg_resource(R.drawable.yellow);
                break;
            case 2:
                setImg_resource(R.drawable.red);
                break;
        }
        setStartDateCalendar(startDateCalendar);
        setEndDateCalendar(endDateCalendar);
        setStartDateStr(dateFormat.format(startDateCalendar.getTime()));
        setStartTimeStr(stf.format(startDateCalendar.getTime()));
        setEndDateStr(dateFormat.format(endDateCalendar.getTime()));
        setEndTimeStr(stf.format(endDateCalendar.getTime()));


    }

    //Getters and Setters
    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public boolean isControlFlag() {
        return controlFlag;
    }

    public void setControlFlag(boolean controlFlag) {
        this.controlFlag = controlFlag;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setImg_resource(int img_resource) {
        this.img_resource = img_resource;
    }

    public int getImg_resource() {
        return img_resource;
    }

    String getEventName(){
        return this.eventName;
    }

    int getStatus(){
        return this.status;
    }

    public String getStartDateStr() {
        return startDateStr;
    }

    public void setStartDateStr(String startDateStr) {
        this.startDateStr = startDateStr;
    }

    public String getStartTimeStr() {
        return startTimeStr;
    }

    public void setStartTimeStr(String startTimeStr) {
        this.startTimeStr = startTimeStr;
    }

    public String getEndDateStr() {
        return endDateStr;
    }

    public void setEndDateStr(String endDateStr) {
        this.endDateStr = endDateStr;
    }

    public String getEndTimeStr() {
        return endTimeStr;
    }

    public void setEndTimeStr(String endTimeStr) {
        this.endTimeStr = endTimeStr;
    }

    public Calendar getStartDateCalendar() {
        return startDateCalendar;
    }

    public void setStartDateCalendar(Calendar startDateCalendar) {
        this.startDateCalendar = startDateCalendar;
    }

    public Calendar getEndDateCalendar() {
        return endDateCalendar;
    }

    public void setEndDateCalendar(Calendar endDateCalendar) {
        this.endDateCalendar = endDateCalendar;
    }



    //Method implementation for making it Parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    public Event(Parcel in){
        this.eventName = in.readString();
        this.status = in.readInt();
        this.img_resource = in.readInt();
        this.startDateStr = in.readString();
        this.startTimeStr = in.readString();
        this.endDateStr = in.readString();
        this.endTimeStr = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(eventName);
        dest.writeInt(status);
        dest.writeInt(img_resource);
        dest.writeString(startDateStr);
        dest.writeString(startTimeStr);
        dest.writeString(endDateStr);
        dest.writeString(endTimeStr);
    }

    public static final Parcelable.Creator<Event> CREATOR = new Parcelable.Creator<Event>() {
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        public Event[] newArray(int size) {
            return new Event[size];

        }
    };
}
