package com.zik.faro.frontend;

import com.zik.faro.data.MinUser;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FriendListHandler {
    private static FriendListHandler friendListHandler = null;

    public static FriendListHandler getInstance(){
        if (friendListHandler != null){
            return friendListHandler;
        }
        synchronized (FriendListHandler.class)
        {
            if (friendListHandler == null){
                friendListHandler = new FriendListHandler();
            }
            return friendListHandler;
        }
    }

    private FriendListHandler(){}

    public FriendAdapter friendAdapter;

    /*
    * Map of friends needed to access friends downloaded from the server in O(1) time. The Key to the
    * Map is the emailID  which returns the MinUser as the value.
    */
    private Map<String, MinUser> friendMap = new ConcurrentHashMap<>();

    private static String TAG = "FriendListHandler";

    private void addFriendToList(MinUser minUser){
        friendAdapter.insert(minUser, 0);
        friendAdapter.notifyDataSetChanged();
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

    public void removeFriendFromListAndMap(String emailID){
        MinUser minUser = friendMap.get(emailID);
        if (minUser == null){
            return;
        }
        friendAdapter.list.remove(minUser);
        friendAdapter.notifyDataSetChanged();
        friendMap.remove(emailID);
    }

    public void clearFriendListAndMap(){
        if (friendAdapter != null){
            friendAdapter.list.clear();
            friendAdapter.notifyDataSetChanged();
        }
        if (friendMap != null){
            friendMap.clear();
        }
    }



}
