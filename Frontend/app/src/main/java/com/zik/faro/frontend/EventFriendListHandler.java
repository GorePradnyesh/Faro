package com.zik.faro.frontend;

import android.content.Context;

import com.zik.faro.data.Event;
import com.zik.faro.data.InviteeList;
import com.zik.faro.data.user.EventInviteStatus;
import com.zik.faro.frontend.faroservice.auth.FaroUserContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EventFriendListHandler {
    private static EventFriendListHandler eventFriendListHandler = null;

    public static EventFriendListHandler getInstance(){
        if (eventFriendListHandler != null){
            return eventFriendListHandler;
        }
        synchronized (EventFriendListHandler.class)
        {
            if (eventFriendListHandler == null){
                eventFriendListHandler = new EventFriendListHandler();
            }
            return eventFriendListHandler;
        }
    }

    private EventFriendListHandler(){}

    public Map<String, EventFriendAdapter> acceptedFriendAdapterMap = new ConcurrentHashMap<>();
    public Map<String, EventFriendAdapter> mayBeFriendAdapterMap = new ConcurrentHashMap<>();
    public Map<String, EventFriendAdapter> invitedFriendAdapterMap = new ConcurrentHashMap<>();
    public Map<String, EventFriendAdapter> declinedFriendAdapterMap = new ConcurrentHashMap<>();


    /*
    * Map of friends needed to access friends downloaded from the server in O(1) time. The Key to the
    * Map is the emailID  which returns the Invitees as the value.
    */
    private Map<String, InviteeList.Invitees> friendMap = new ConcurrentHashMap<>();

    private static String TAG = "EventFriendListHandler";


    public EventFriendAdapter getAcceptedFriendAdapter(String eventID, Context context){
        EventFriendAdapter eventFriendAdapter = acceptedFriendAdapterMap.get(eventID);
        if (eventFriendAdapter == null){
            eventFriendAdapter = new EventFriendAdapter(context, R.layout.friend_row_style);
            acceptedFriendAdapterMap.put(eventID, eventFriendAdapter);
        }
        return eventFriendAdapter;
    }

    public EventFriendAdapter getMayBeFriendAdapter(String eventID, Context context){
        EventFriendAdapter eventFriendAdapter = mayBeFriendAdapterMap.get(eventID);
        if (eventFriendAdapter == null){
            eventFriendAdapter = new EventFriendAdapter(context, R.layout.friend_row_style);
            mayBeFriendAdapterMap.put(eventID, eventFriendAdapter);
        }
        return eventFriendAdapter;
    }

    public EventFriendAdapter getInvitedFriendAdapter(String eventID, Context context){
        EventFriendAdapter eventFriendAdapter = invitedFriendAdapterMap.get(eventID);
        if (eventFriendAdapter == null){
            eventFriendAdapter = new EventFriendAdapter(context, R.layout.friend_row_style);
            invitedFriendAdapterMap.put(eventID, eventFriendAdapter);
        }
        return eventFriendAdapter;
    }

    public EventFriendAdapter getDeclinedFriendAdapter(String eventID, Context context){
        EventFriendAdapter eventFriendAdapter = declinedFriendAdapterMap.get(eventID);
        if (eventFriendAdapter == null){
            eventFriendAdapter = new EventFriendAdapter(context, R.layout.friend_row_style);
            declinedFriendAdapterMap.put(eventID, eventFriendAdapter);
        }
        return eventFriendAdapter;
    }

    private EventFriendAdapter getEventFriendAdapter(String eventID, InviteeList.Invitees invitees, Context context){
        switch (invitees.getInviteStatus()){
            case ACCEPTED:
                return getAcceptedFriendAdapter(eventID, context);
            case MAYBE:
                return getMayBeFriendAdapter(eventID, context);
            case INVITED:
                return getInvitedFriendAdapter(eventID, context);
            case DECLINED:
                return getDeclinedFriendAdapter(eventID, context);
            default:
                return getInvitedFriendAdapter(eventID, context);
        }
    }

    public EventFriendAdapter getEventFriendAdapter(String eventID, String status, Context context){
        switch (status){
            case "Going":
                return getAcceptedFriendAdapter(eventID, context);
            case "Maybe":
                return getMayBeFriendAdapter(eventID, context);
            case "Invited":
                return getInvitedFriendAdapter(eventID, context);
            case "Not Going":
                return getDeclinedFriendAdapter(eventID, context);
            default:
                return getInvitedFriendAdapter(eventID, context);
        }
    }

    private void conditionallyAddFriendToList(String eventID, InviteeList.Invitees invitees, Context context){
        EventFriendAdapter eventFriendAdapter = getEventFriendAdapter(eventID, invitees, context);
        eventFriendAdapter.insert(invitees, 0);
        eventFriendAdapter.notifyDataSetChanged();
    }

    public int getAcceptedFriendCount(String eventID, Context context){
        return getAcceptedFriendAdapter(eventID, context).getCount();
    }

    public int getInvitedFriendCount(String eventID, Context context){
        return getInvitedFriendAdapter(eventID, context).getCount();
    }

    public int getMayBeFriendCount(String eventID, Context context){
        return getMayBeFriendAdapter(eventID, context).getCount();
    }

    public int getDeclinedFriendCount(String eventID, Context context){
        return getDeclinedFriendAdapter(eventID, context).getCount();
    }

    public void addFriendToListAndMap(String eventID, InviteeList.Invitees invitees, Context context){
        /*
         * If the received Invitees is already present in the local database, then we need to delete that and
         * update it with the newly received Invitees.
         */
        removeFriendFromListAndMap(eventID, invitees.getEmail(), context);
        conditionallyAddFriendToList(eventID, invitees, context);
        friendMap.put(invitees.getEmail(), invitees);
    }


    public void addDownloadedFriendsToListAndMap(String eventID, InviteeList inviteeList, Context context){
        for(Map.Entry<String, InviteeList.Invitees> entry: inviteeList.getUserStatusMap().entrySet()){
            addFriendToListAndMap(eventID, entry.getValue(), context);
        }
    }

    public void removeFriendFromListAndMap(String eventID, String emailID, Context context){
        InviteeList.Invitees invitees = friendMap.get(emailID);
        if (invitees == null){
            return;
        }

        EventFriendAdapter eventFriendAdapter = getEventFriendAdapter(eventID, invitees, context);
        eventFriendAdapter.list.remove(invitees);
        eventFriendAdapter.notifyDataSetChanged();
        friendMap.remove(emailID);
    }

    public void clearFriendListAndMap(String eventID, Context context){
        EventFriendAdapter acceptedFriendAdapter = getAcceptedFriendAdapter(eventID, context);
        EventFriendAdapter invitedFriendAdapter = getInvitedFriendAdapter(eventID, context);
        EventFriendAdapter mayBeFriendAdapter = getMayBeFriendAdapter(eventID, context);
        EventFriendAdapter declinedFriendAdapter = getDeclinedFriendAdapter(eventID, context);
        if (acceptedFriendAdapter != null){
            acceptedFriendAdapter.list.clear();
            acceptedFriendAdapter.notifyDataSetChanged();
        }
        if (invitedFriendAdapter != null){
            invitedFriendAdapter.list.clear();
            invitedFriendAdapter.notifyDataSetChanged();
        }
        if (mayBeFriendAdapter != null){
            mayBeFriendAdapter.list.clear();
            mayBeFriendAdapter.notifyDataSetChanged();
        }
        if (declinedFriendAdapter != null){
            declinedFriendAdapter.list.clear();
            declinedFriendAdapter.notifyDataSetChanged();
        }
        if (friendMap != null){
            friendMap.clear();
        }
    }

    boolean isFriendInvitedToEvent(String emailID){
        if (friendMap.containsKey(emailID)){
            return true;
        }else {
            return false;
        }
    }

    boolean isFriendComingToEvent(String emailID){
        InviteeList.Invitees invitees = friendMap.get(emailID);
        if (invitees.getInviteStatus().equals(EventInviteStatus.ACCEPTED)){
            return true;
        }else {
            return false;
        }
    }

    public String getFriendFullNameFromID(String emailID) {
        String friendFirstName = null;
        String friendLastName = null;
        String friendFullName = null;

        FaroUserContext faroUserContext = FaroUserContext.getInstance();
        String myUserId = faroUserContext.getEmail();

        //TODO Cache my info and then retrieve FirstName and Last Name from there
        if (emailID.equals(myUserId)){
            return emailID;
        }
        InviteeList.Invitees invitees = friendMap.get(emailID);
        if (invitees != null) {
            friendFirstName = invitees.getFirstName();
            friendLastName = invitees.getLastName();
            if (friendLastName != null) {
                friendFullName = friendFirstName + " " + friendLastName;
            }else{
                friendFullName = friendFirstName;
            }
        }
        return friendFullName;
    }
}
