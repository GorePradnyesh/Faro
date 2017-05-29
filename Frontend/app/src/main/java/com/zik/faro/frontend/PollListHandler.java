package com.zik.faro.frontend;

import android.content.Context;

import com.google.gson.Gson;
import com.zik.faro.data.ObjectStatus;
import com.zik.faro.data.Poll;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class PollListHandler {
    /*
     * This is a Singleton class
     */
    private static PollListHandler pollListHandler = null;
    public static PollListHandler getInstance(){
        if (pollListHandler != null){
            return pollListHandler;
        }
        synchronized (PollListHandler.class)
        {
            if(pollListHandler == null)
                pollListHandler = new PollListHandler();
            return pollListHandler;
        }
    }

    private PollListHandler(){}

    private static final int MAX_POLLS_PAGE_SIZE = 100;
    private static final int MAX_TOTAL_POLLS_IN_CACHE = 200;
    public static final int MAX_TOTAL_POLLS_PER_EVENT = 200;


    public Map<String, PollAdapter>openPollsAdapterMap = new ConcurrentHashMap<>();
    public Map<String, PollAdapter>closedPollsAdapterMap = new ConcurrentHashMap<>();

    /*
    * Map of Polls needed to access polls downloaded from the server in O(1) time. The Key to the
    * Map is the pollID String which returns the Poll as the value
    */
    private Map<String, Poll> pollMap = new ConcurrentHashMap<>();

    public Poll getPollCloneFromMap(String pollID){
        Poll poll = pollMap.get(pollID);
        Gson gson = new Gson();
        String json = gson.toJson(poll);
        Poll clonePoll = gson.fromJson(json, Poll.class);
        return clonePoll;
    }


    //TODO Function call to remove items from the List and Map when user keeps scrolling and caches
    // lot of polls. Have a Max limit on number of polls we will cache else will use up a lot of
    // memory and battery. Use MAX_TOTAL_POLLS_IN_CACHE to set the max polls we will cache

    public PollAdapter getOpenPollAdapter(String eventID, Context context){
        PollAdapter openPollAdapter = openPollsAdapterMap.get(eventID);
        if (openPollAdapter == null){
            openPollAdapter = new PollAdapter(context, R.layout.poll_list_page_row_style);
            openPollsAdapterMap.put(eventID, openPollAdapter);
        }
        return openPollAdapter;
    }

    public PollAdapter getClosedPollAdapter(String eventID, Context context){
        PollAdapter closedPollAdapter = closedPollsAdapterMap.get(eventID);
        if (closedPollAdapter == null){
            closedPollAdapter = new PollAdapter(context, R.layout.poll_list_page_row_style);
            closedPollsAdapterMap.put(eventID, closedPollAdapter);
        }
        return closedPollAdapter;
    }


    public void addPollToListAndMap(String eventID, Poll poll, Context context){
        removePollFromListAndMap(eventID, poll, context);
        conditionallyAddNewPollToList(eventID, poll, context);
        pollMap.put(poll.getId(), poll);
    }

    private void conditionallyAddNewPollToList(String eventID, Poll poll, Context context) {
        PollAdapter pollAdapter;
        pollAdapter = getPollAdapter(eventID, poll, context);
        if(pollAdapter != null) {
            pollAdapter.insert(poll, 0);
            pollAdapter.notifyDataSetChanged();
        }
    }

    public void addDownloadedPollsToListAndMap(String eventID, List<Poll> pollList, Context context){
        for (int i = 0; i < pollList.size(); i++){
            Poll poll = pollList.get(i);
            addPollToListAndMap(eventID, poll, context);
        }
        PollAdapter openPollsAdapter = getOpenPollAdapter(eventID, context);
        PollAdapter closedPollsAdapter = getClosedPollAdapter(eventID, context);
        openPollsAdapter.notifyDataSetChanged();
        closedPollsAdapter.notifyDataSetChanged();
    }

    public int getCombinedListSize(String eventID, Context context){
        PollAdapter openPollsAdapter = getOpenPollAdapter(eventID, context);
        PollAdapter closedPollsAdapter = getClosedPollAdapter(eventID, context);
        return openPollsAdapter.list.size()+ closedPollsAdapter.list.size();
    }

    private PollAdapter getPollAdapter(String eventID, Poll poll, Context context){
        switch(poll.getStatus()){
            case OPEN:
                return getOpenPollAdapter(eventID, context);
            case CLOSED:
                return getClosedPollAdapter(eventID, context);
            default:
                //TODO: How to catch this condition? This should never occur?
                return null;
        }
    }

    public void removePollFromList(String pollID, List <Poll> pollList){
        for (int i = 0; i < pollList.size(); i++){
            Poll poll = pollList.get(i);
            if (poll.getId().equals(pollID)){
                pollList.remove(poll);
                return;
            }
        }
    }

    public void removePollFromListAndMap(String eventID, Poll poll, Context context){
        PollAdapter pollAdapter;
        pollAdapter = getPollAdapter(eventID, poll, context);
        if(pollAdapter != null) {
            removePollFromList(poll.getId(), pollAdapter.list);
            pollAdapter.notifyDataSetChanged();
            pollMap.remove(poll.getId());
        }
    }

    public void clearPollListsAndMap(String eventID, Context context){
        PollAdapter openPollsAdapter = getOpenPollAdapter(eventID, context);
        PollAdapter closedPollsAdapter = getClosedPollAdapter(eventID, context);
        if (openPollsAdapter != null) {
            openPollsAdapter.list.clear();
            openPollsAdapterMap.remove(eventID);
        }
        if (closedPollsAdapter != null) {
            closedPollsAdapter.list.clear();
            closedPollsAdapterMap.remove(eventID);
        }
        if (pollMap != null) {
            pollMap.clear();
        }
    }

    // Special handling for notification done. This is called from Polllanding page only if it was
    // opened through a notification.
    public void removeNotificationPollFromListAndMap(String eventID, String pollID, Context context){
        Poll poll = pollMap.get(pollID);
        removePollFromListAndMap(eventID, poll, context);
        clearPollAdaptersIfEmpty(eventID, poll, context);
    }

    private void clearPollAdaptersIfEmpty(String eventID, Poll poll, Context context){
        PollAdapter openPollsAdapter = getOpenPollAdapter(eventID, context);
        PollAdapter closedPollsAdapter = getClosedPollAdapter(eventID, context);
        if (openPollsAdapter != null) {
            if (openPollsAdapter.list.isEmpty()) {
                openPollsAdapterMap.remove(eventID);
            }
        }
        if (closedPollsAdapter != null) {
            if (closedPollsAdapter.list.isEmpty()) {
                closedPollsAdapterMap.remove(eventID);
            }
        }
    }
}