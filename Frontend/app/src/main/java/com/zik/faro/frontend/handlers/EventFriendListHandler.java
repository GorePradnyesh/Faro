package com.zik.faro.frontend.handlers;

import android.content.Context;

import com.google.gson.Gson;
import com.zik.faro.data.BaseEntity;
import com.zik.faro.data.Event;
import com.zik.faro.data.InviteeList;
import com.zik.faro.data.user.EventInviteStatus;
import com.zik.faro.frontend.R;
import com.zik.faro.frontend.ui.adapters.EventFriendAdapter;
import com.zik.faro.frontend.faroservice.auth.FaroUserContext;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EventFriendListHandler{
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
    * Map is the emailId  which returns the Invitees as the value.
    */
    private Map<String, InviteeList.Invitees> friendMap = new ConcurrentHashMap<>();

    private static String TAG = "EventFriendListHandler";


    public EventFriendAdapter getAcceptedFriendAdapter(String eventId, Context context){
        EventFriendAdapter eventFriendAdapter = acceptedFriendAdapterMap.get(eventId);
        if (eventFriendAdapter == null){
            eventFriendAdapter = new EventFriendAdapter(context, R.layout.friend_row_style);
            acceptedFriendAdapterMap.put(eventId, eventFriendAdapter);
        }
        return eventFriendAdapter;
    }

    public EventFriendAdapter getMayBeFriendAdapter(String eventId, Context context){
        EventFriendAdapter eventFriendAdapter = mayBeFriendAdapterMap.get(eventId);
        if (eventFriendAdapter == null){
            eventFriendAdapter = new EventFriendAdapter(context, R.layout.friend_row_style);
            mayBeFriendAdapterMap.put(eventId, eventFriendAdapter);
        }
        return eventFriendAdapter;
    }

    public EventFriendAdapter getInvitedFriendAdapter(String eventId, Context context){
        EventFriendAdapter eventFriendAdapter = invitedFriendAdapterMap.get(eventId);
        if (eventFriendAdapter == null){
            eventFriendAdapter = new EventFriendAdapter(context, R.layout.friend_row_style);
            invitedFriendAdapterMap.put(eventId, eventFriendAdapter);
        }
        return eventFriendAdapter;
    }

    public EventFriendAdapter getDeclinedFriendAdapter(String eventId, Context context){
        EventFriendAdapter eventFriendAdapter = declinedFriendAdapterMap.get(eventId);
        if (eventFriendAdapter == null){
            eventFriendAdapter = new EventFriendAdapter(context, R.layout.friend_row_style);
            declinedFriendAdapterMap.put(eventId, eventFriendAdapter);
        }
        return eventFriendAdapter;
    }

    private EventFriendAdapter getEventFriendAdapter(String eventId, InviteeList.Invitees invitees, Context context){
        switch (invitees.getInviteStatus()){
            case ACCEPTED:
                return getAcceptedFriendAdapter(eventId, context);
            case MAYBE:
                return getMayBeFriendAdapter(eventId, context);
            case INVITED:
                return getInvitedFriendAdapter(eventId, context);
            case DECLINED:
                return getDeclinedFriendAdapter(eventId, context);
            default:
                return getInvitedFriendAdapter(eventId, context);
        }
    }

    public EventFriendAdapter getEventFriendAdapter(String eventId, String status, Context context){
        switch (status){
            case "Going":
                return getAcceptedFriendAdapter(eventId, context);
            case "Maybe":
                return getMayBeFriendAdapter(eventId, context);
            case "Invited":
                return getInvitedFriendAdapter(eventId, context);
            case "Not Going":
                return getDeclinedFriendAdapter(eventId, context);
            default:
                return getInvitedFriendAdapter(eventId, context);
        }
    }

    private void conditionallyAddFriendToList(String eventId, InviteeList.Invitees invitees, Context context){
        EventFriendAdapter eventFriendAdapter = getEventFriendAdapter(eventId, invitees, context);
        eventFriendAdapter.insert(invitees, 0);
        eventFriendAdapter.notifyDataSetChanged();
    }

    public int getAcceptedFriendCount(String eventId, Context context){
        return getAcceptedFriendAdapter(eventId, context).getCount();
    }

    public int getInvitedFriendCount(String eventId, Context context){
        return getInvitedFriendAdapter(eventId, context).getCount();
    }

    public int getMayBeFriendCount(String eventId, Context context){
        return getMayBeFriendAdapter(eventId, context).getCount();
    }

    public int getDeclinedFriendCount(String eventId, Context context){
        return getDeclinedFriendAdapter(eventId, context).getCount();
    }

    public void addFriendToListAndMap(String eventId, InviteeList.Invitees invitees, Context context){
        /*
         * If the received Invitees is already present in the local database, then we need to delete that and
         * update it with the newly received Invitees.
         */
        removeFriendFromListAndMap(eventId, invitees.getEmail(), context);
        conditionallyAddFriendToList(eventId, invitees, context);
        friendMap.put(invitees.getEmail(), invitees);
    }

    public void removeAllFromListAndMap (EventFriendAdapter eventFriendAdapter) {
        for (Iterator<InviteeList.Invitees> iterator = eventFriendAdapter.list.iterator(); iterator.hasNext();){
            InviteeList.Invitees invitees = iterator.next();
            friendMap.remove(invitees.getEmail());
            iterator.remove();
        }
        eventFriendAdapter.notifyDataSetChanged();
    }

    public void removeAllFriendsFromListAndMapForEvent (String eventId, Context context) {
        removeAllFromListAndMap(getAcceptedFriendAdapter(eventId, context));
        removeAllFromListAndMap(getMayBeFriendAdapter(eventId, context));
        removeAllFromListAndMap(getInvitedFriendAdapter(eventId, context));
        removeAllFromListAndMap(getDeclinedFriendAdapter(eventId, context));
    }


    public void addDownloadedFriendsToListAndMap(String eventId, InviteeList inviteeList, Context context){
        removeAllFriendsFromListAndMapForEvent(eventId, context);
        for(Map.Entry<String, InviteeList.Invitees> entry: inviteeList.getUserStatusMap().entrySet()){
            addFriendToListAndMap(eventId, entry.getValue(), context);
        }
    }

    public void removeFriendFromListAndMap(String eventId, String emailId, Context context){
        InviteeList.Invitees invitees = friendMap.get(emailId);
        if (invitees == null){
            return;
        }

        EventFriendAdapter eventFriendAdapter = getEventFriendAdapter(eventId, invitees, context);
        eventFriendAdapter.list.remove(invitees);
        eventFriendAdapter.notifyDataSetChanged();
        friendMap.remove(emailId);
    }

    public void clearEverything () {
        if (acceptedFriendAdapterMap != null)
            acceptedFriendAdapterMap.clear();
        if (mayBeFriendAdapterMap != null)
            mayBeFriendAdapterMap.clear();
        if (invitedFriendAdapterMap != null)
            invitedFriendAdapterMap.clear();
        if (declinedFriendAdapterMap != null)
            declinedFriendAdapterMap.clear();
        if (friendMap != null)
            friendMap.clear();
    }

    /*public void clearFriendListAndMap(String eventId, Context context){
        EventFriendAdapter acceptedFriendAdapter = getAcceptedFriendAdapter(eventId, context);
        EventFriendAdapter invitedFriendAdapter = getInvitedFriendAdapter(eventId, context);
        EventFriendAdapter mayBeFriendAdapter = getMayBeFriendAdapter(eventId, context);
        EventFriendAdapter declinedFriendAdapter = getDeclinedFriendAdapter(eventId, context);
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
    }*/

    public boolean isFriendInvitedToEvent(String emailId){
        if (friendMap.containsKey(emailId)){
            return true;
        }else {
            return false;
        }
    }

    boolean isFriendComingToEvent(String emailId){
        InviteeList.Invitees invitees = friendMap.get(emailId);
        if (invitees.getInviteStatus().equals(EventInviteStatus.ACCEPTED)){
            return true;
        }else {
            return false;
        }
    }

    public String getFriendFullNameFromID(String emailId) {
        String friendFirstName = null;
        String friendLastName = null;
        String friendFullName = null;

        FaroUserContext faroUserContext = FaroUserContext.getInstance();
        String myUserId = faroUserContext.getEmail();

        //TODO Cache my info and then retrieve FirstName and Last Name from there
        if (emailId.equals(myUserId)){
            return emailId;
        }
        InviteeList.Invitees invitees = friendMap.get(emailId);
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

    public InviteeList.Invitees getInviteesCloneFromMap(String emailId){
        InviteeList.Invitees invitee = friendMap.get(emailId);
        Gson gson = new Gson();
        String json = gson.toJson(invitee);
        return gson.fromJson(json, InviteeList.Invitees.class);
    }
}
