package com.example.nakulshah.FrontEnd;

/**
 * Created by nakulshah on 1/27/15.
 */
public class EventTimes {

    int startHour = 0;
    int startMinute = 0;
    int EndHour = 0;
    int EndMinute = 0;

    public int getStartHour() {
        return startHour;
    }

    public void setStartHour(int startHour) {
        this.startHour = startHour;
    }

    public int getStartMinute() {
        return startMinute;
    }

    public void setStartMinute(int startMinute) {
        this.startMinute = startMinute;
    }

    public int getEndHour() {
        return EndHour;
    }

    public void setEndHour(int endHour) {
        EndHour = endHour;
    }

    public int getEndMinute() {
        return EndMinute;
    }

    public void setEndMinute(int endMinute) {
        EndMinute = endMinute;
    }



    public EventTimes(int startHour, int startMinute, int endHour, int endMinute) {
        this.startHour = startHour;
        this.startMinute = startMinute;
        EndHour = endHour;
        EndMinute = endMinute;
    }

    public void setStartTime(int startHour, int startMinute){
        setStartHour(startHour);
        setStartMinute(startMinute);
    }

    public void setEndTime(int endHour, int endMinute){
        setEndHour(endHour);
        setEndMinute(endMinute);
    }

    public void setEndTimetoStartTime(){
        setEndHour(getStartHour());
        setEndMinute(getStartMinute());
    }
}
