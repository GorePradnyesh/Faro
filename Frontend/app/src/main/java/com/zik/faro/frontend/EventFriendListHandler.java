package com.zik.faro.frontend;

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

    public EventFriendAdapter acceptedFriendAdapter;
    public EventFriendAdapter notAcceptedFriendAdapter;


    /*
    * Map of friends needed to access friends downloaded from the server in O(1) time. The Key to the
    * Map is the emailID  which returns the Invitees as the value.
    */
    private Map<String, InviteeList.Invitees> friendMap = new ConcurrentHashMap<>();

    private static String TAG = "EventFriendListHandler";

    private EventFriendAdapter getEventFriendAdapter(InviteeList.Invitees invitees){
        switch (invitees.getInviteStatus()){
            case ACCEPTED:
                return acceptedFriendAdapter;
            default:
                return notAcceptedFriendAdapter;
        }
    }

    private void conditionallyAddFriendToList(InviteeList.Invitees invitees){
        EventFriendAdapter eventFriendAdapter = getEventFriendAdapter(invitees);
        eventFriendAdapter.insert(invitees, 0);
        eventFriendAdapter.notifyDataSetChanged();
    }

    public void addFriendToListAndMap(InviteeList.Invitees invitees){
        /*
         * If the received Invitees is already present in the local database, then we need to delete that and
         * update it with the newly received Invitees.
         */
        removeFriendFromListAndMap(invitees.getEmail());
        conditionallyAddFriendToList(invitees);
        friendMap.put(invitees.getEmail(), invitees);
    }


    public void addDownloadedFriendsToListAndMap(InviteeList inviteeList){
        for(Map.Entry<String, InviteeList.Invitees> entry: inviteeList.getUserStatusMap().entrySet()){
            addFriendToListAndMap(entry.getValue());
        }
    }

    public void removeFriendFromListAndMap(String emailID){
        InviteeList.Invitees invitees = friendMap.get(emailID);
        if (invitees == null){
            return;
        }

        EventFriendAdapter eventFriendAdapter = getEventFriendAdapter(invitees);
        eventFriendAdapter.list.remove(invitees);
        eventFriendAdapter.notifyDataSetChanged();
        friendMap.remove(emailID);
    }

    public void clearFriendListAndMap(){
        if (acceptedFriendAdapter != null){
            acceptedFriendAdapter.list.clear();
            acceptedFriendAdapter.notifyDataSetChanged();
        }
        if (notAcceptedFriendAdapter != null){
            notAcceptedFriendAdapter.list.clear();
            notAcceptedFriendAdapter.notifyDataSetChanged();
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
