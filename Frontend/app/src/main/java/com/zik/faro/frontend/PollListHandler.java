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
    * Map is the pollId String which returns the Poll as the value
    */
    private Map<String, Poll> pollMap = new ConcurrentHashMap<>();

    public Poll getPollCloneFromMap(String pollId){
        Poll poll = pollMap.get(pollId);
        Gson gson = new Gson();
        String json = gson.toJson(poll);
        Poll clonePoll = gson.fromJson(json, Poll.class);
        return clonePoll;
    }

    public Poll getOriginalPollFromMap(String pollId){
        Poll poll = pollMap.get(pollId);
        return poll;
    }


    //TODO Function call to remove items from the List and Map when user keeps scrolling and caches
    // lot of polls. Have a Max limit on number of polls we will cache else will use up a lot of
    // memory and battery. Use MAX_TOTAL_POLLS_IN_CACHE to set the max polls we will cache

    public PollAdapter getOpenPollAdapter(String eventId, Context context){
        PollAdapter openPollAdapter = openPollsAdapterMap.get(eventId);
        if (openPollAdapter == null){
            openPollAdapter = new PollAdapter(context, R.layout.poll_list_page_row_style);
            openPollsAdapterMap.put(eventId, openPollAdapter);
        }
        return openPollAdapter;
    }

    public PollAdapter getClosedPollAdapter(String eventId, Context context){
        PollAdapter closedPollAdapter = closedPollsAdapterMap.get(eventId);
        if (closedPollAdapter == null){
            closedPollAdapter = new PollAdapter(context, R.layout.poll_list_page_row_style);
            closedPollsAdapterMap.put(eventId, closedPollAdapter);
        }
        return closedPollAdapter;
    }


    public void addPollToListAndMap(String eventId, Poll poll, Context context){
        removePollFromListAndMap(eventId, poll, context);
        conditionallyAddNewPollToList(eventId, poll, context);
        pollMap.put(poll.getId(), poll);
    }

    private void conditionallyAddNewPollToList(String eventId, Poll poll, Context context) {
        PollAdapter pollAdapter;
        pollAdapter = getPollAdapter(eventId, poll, context);
        if(pollAdapter != null) {
            pollAdapter.insert(poll, 0);
            pollAdapter.notifyDataSetChanged();
        }
    }

    public void addDownloadedPollsToListAndMap(String eventId, List<Poll> pollList, Context context){
        for (int i = 0; i < pollList.size(); i++){
            Poll poll = pollList.get(i);
            addPollToListAndMap(eventId, poll, context);
        }
        PollAdapter openPollsAdapter = getOpenPollAdapter(eventId, context);
        PollAdapter closedPollsAdapter = getClosedPollAdapter(eventId, context);
        openPollsAdapter.notifyDataSetChanged();
        closedPollsAdapter.notifyDataSetChanged();
    }

    public int getCombinedListSize(String eventId, Context context){
        PollAdapter openPollsAdapter = getOpenPollAdapter(eventId, context);
        PollAdapter closedPollsAdapter = getClosedPollAdapter(eventId, context);
        return openPollsAdapter.list.size()+ closedPollsAdapter.list.size();
    }

    private PollAdapter getPollAdapter(String eventId, Poll poll, Context context){
        switch(poll.getStatus()){
            case OPEN:
                return getOpenPollAdapter(eventId, context);
            case CLOSED:
                return getClosedPollAdapter(eventId, context);
            default:
                return getClosedPollAdapter(eventId, context);
        }
    }

    public void removePollFromList(String pollId, List <Poll> pollList){
        for (int i = 0; i < pollList.size(); i++){
            Poll poll = pollList.get(i);
            if (poll.getId().equals(pollId)){
                pollList.remove(poll);
                return;
            }
        }
    }

    public void removePollFromListAndMap(String eventId, Poll poll, Context context){
        PollAdapter pollAdapter;
        pollAdapter = getPollAdapter(eventId, poll, context);
        if(pollAdapter != null) {
            removePollFromList(poll.getId(), pollAdapter.list);
            pollAdapter.notifyDataSetChanged();
            pollMap.remove(poll.getId());
        }
    }

    public void clearEverything() {
        if (openPollsAdapterMap != null)
            openPollsAdapterMap.clear();
        if (closedPollsAdapterMap != null)
            closedPollsAdapterMap.clear();
        if (pollMap != null)
            pollMap.clear();
    }

    /*public void clearPollListsAndMap(String eventId, Context context){
        PollAdapter openPollsAdapter = getOpenPollAdapter(eventId, context);
        PollAdapter closedPollsAdapter = getClosedPollAdapter(eventId, context);
        if (openPollsAdapter != null) {
            openPollsAdapter.list.clear();
            openPollsAdapterMap.remove(eventId);
        }
        if (closedPollsAdapter != null) {
            closedPollsAdapter.list.clear();
            closedPollsAdapterMap.remove(eventId);
        }
        if (pollMap != null) {
            pollMap.clear();
        }
    }*/

    private void clearPollAdaptersIfEmpty(String eventId, Poll poll, Context context){
        PollAdapter openPollsAdapter = getOpenPollAdapter(eventId, context);
        PollAdapter closedPollsAdapter = getClosedPollAdapter(eventId, context);
        if (openPollsAdapter != null) {
            if (openPollsAdapter.list.isEmpty()) {
                openPollsAdapterMap.remove(eventId);
            }
        }
        if (closedPollsAdapter != null) {
            if (closedPollsAdapter.list.isEmpty()) {
                closedPollsAdapterMap.remove(eventId);
            }
        }
    }
}
