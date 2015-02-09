package com.example.nakulshah.listviewyoutube;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

/**
 * Created by nakulshah on 1/8/15.
 */
public class EventGenerator{


    private static EventGenerator evntGen = null;
    public ArrayList <Event> acceptedEventArrayList = new ArrayList<Event>();
    public ArrayList <Event> notAcceptedEventArrayList = new ArrayList<Event>();
    String[] events = {"Shasta", "Tahoe", "Reno", "Vegas", "New York", "Zion National Park", "Chicago", "Mono Lake","India","Camping", "Birthday", "Movie", "Hiking", "Lounge", "House Warming", "Cricket", "Soccer"};

    private EventGenerator(){
        //for (int i = 0; i < events.length; i++) {
            //createAndInsertEvent(events[i],i);
        //}
    }

    public static EventGenerator getInstance(){
        if (evntGen != null){
            return evntGen;
        }
        synchronized (EventGenerator.class)
        {
            if(evntGen == null)
                evntGen = new EventGenerator();
            return evntGen;
        }
    }



    /*void createAndInsertEvent(String eventName, int position){
        int status = position % 3;
        Event E = new Event(eventName, status);
        if (status == 2){
            notAcceptedEventArrayList.add(E);
        }else{
            acceptedEventArrayList.add(E);
        }
    }*/

    void addNewEvent(Event E){
        if (E.getStatus() == 2){
            addNewEventToList(notAcceptedEventArrayList, E);
        }else{
            addNewEventToList(acceptedEventArrayList, E);
        }
    }

    private void addNewEventToList(ArrayList list, Event E){
        Event tempEvent = null;
        Calendar tempCalendar = null;
        Calendar eventCalendar = null;
        int i = 0;

        if(list.size() == 0){
            list.add(i, E);
            return;
        }

        for(i = 0; i < list.size(); i++){
            tempEvent = (Event)list.get(i);
            tempCalendar = tempEvent.getStartDateCalendar();
            eventCalendar = E.getStartDateCalendar();

            //Insert only if new event is before temp event
            if(eventCalendar.compareTo(tempCalendar) == -1){
                break;
            }
        }
        list.add(i, E);
    }

    public int getAcceptedEventCount(){
        return acceptedEventArrayList.size();
    }

    public int getNotAcceptedEventCount(){
        return notAcceptedEventArrayList.size();
    }

    public Event getAcceptedEventAtPosition(int position) {
        return acceptedEventArrayList.get(position);
    }

    public Event getNotAcceptedEventAtPosition(int position) {
        return notAcceptedEventArrayList.get(position);
    }

    String getEventNameAtPosition (int position){
        return acceptedEventArrayList.get(position).getEventName();
    }

    int getEventImageAtPosition (int position) {
        return acceptedEventArrayList.get(position).getImg_resource();
    }

    int eventCount(){
        return events.length;
    }


    /*public EventGenerator(Parcel in){
        this.acceptedEventArrayList = new ArrayList<Event>();
        in.readTypedList(this.acceptedEventArrayList, Event.CREATOR);
        this.notAcceptedEventArrayList = new ArrayList<Event>();
        in.readTypedList(this.notAcceptedEventArrayList, Event.CREATOR);
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(acceptedEventArrayList);
        dest.writeTypedList(notAcceptedEventArrayList);
    }

    public static final Parcelable.Creator<EventGenerator> CREATOR = new Parcelable.Creator<EventGenerator>() {

        public EventGenerator createFromParcel(Parcel in) {
            return new EventGenerator(in);
        }

        public EventGenerator[] newArray(int size) {
            return new EventGenerator[size];
        }
    };*/

}
