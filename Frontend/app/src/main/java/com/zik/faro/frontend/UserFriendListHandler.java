package com.zik.faro.frontend;

import com.google.gson.Gson;
import com.zik.faro.data.Event;
import com.zik.faro.data.EventInviteStatusWrapper;
import com.zik.faro.data.MinUser;
import com.zik.faro.frontend.faroservice.auth.FaroUserContext;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserFriendListHandler {
    private static UserFriendListHandler userFriendListHandler = null;

    public static UserFriendListHandler getInstance(){
        if (userFriendListHandler != null){
            return userFriendListHandler;
        }

        synchronized (UserFriendListHandler.class) {
            if (userFriendListHandler == null){
                userFriendListHandler = new UserFriendListHandler();
            }
            return userFriendListHandler;
        }
    }

    private UserFriendListHandler(){}

    public UserFriendAdapter userFriendAdapter;

    /*
    * Map of friends needed to access friends downloaded from the server in O(1) time. The Key to the
    * Map is the emailId  which returns the MinUser as the value.
    */
    private Map<String, MinUser> friendMap = new ConcurrentHashMap<>();

    private static String TAG = "UserFriendListHandler";

    private void addFriendToList(MinUser minUser){
        userFriendAdapter.insert(minUser, 0);
        userFriendAdapter.notifyDataSetChanged();
    }

    public void addFriendToListAndMap(MinUser minUser){
        /*
         * If the received minUser is already present in the local database, then we need to delete that and
         * update it with the newly received minUser.
         */
        removeFriendFromListAndMap(minUser.getEmail());
        addFriendToList(minUser);
        friendMap.put(minUser.getEmail(), minUser);
    }

    public void addDownloadedFriendsToListAndMap(List<MinUser> minUserList){
        for (int i = 0; i < minUserList.size(); i++){
            MinUser minUser = minUserList.get(i);
            addFriendToListAndMap(minUser);
        }
    }

    public void removeFriendFromListAndMap(String emailId){
        MinUser minUser = friendMap.get(emailId);
        if (minUser == null){
            return;
        }
        userFriendAdapter.list.remove(minUser);
        userFriendAdapter.notifyDataSetChanged();
        friendMap.remove(emailId);
    }

    public void clearFriendListAndMap(){
        if (userFriendAdapter != null){
            userFriendAdapter.list.clear();
            userFriendAdapter.notifyDataSetChanged();
        }
        if (friendMap != null){
            friendMap.clear();
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
        MinUser minUser = friendMap.get(emailId);
        if (minUser != null) {
            friendFirstName = minUser.getFirstName();
            friendLastName = minUser.getLastName();
            if (friendLastName != null) {
                friendFullName = friendFirstName + " " + friendLastName;
            }else{
                friendFullName = friendFirstName;
            }
        }
        return friendFullName;
    }

    public MinUser getMinUserCloneFromMap(String emailId){
        MinUser minUser = friendMap.get(emailId);
        Gson gson = new Gson();
        String json = gson.toJson(minUser);
        MinUser cloneMinUser = gson.fromJson(json, MinUser.class);
        return cloneMinUser;
    }
}
