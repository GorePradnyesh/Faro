package com.zik.faro.frontend;

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
            if(pollListHandler == null) {
                pollListHandler = new PollListHandler();
            }
            return pollListHandler;
        }
    }

    private PollListHandler(){}

    private static final int MAX_POLLS_PAGE_SIZE = 100;
    private static final int MAX_TOTAL_POLLS_IN_CACHE = 200;
    public static final int MAX_TOTAL_POLLS_PER_EVENT = 200;


    public PollAdapter openPollsAdapter;
    public PollAdapter closedPollsAdapter;

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



    public void addPollToListAndMap(Poll poll){
        removePollFromListAndMap(poll);
        conditionallyAddNewPollToList(poll);
        pollMap.put(poll.getId(), poll);
    }

    private void conditionallyAddNewPollToList(Poll poll) {
        PollAdapter pollAdapter;
        pollAdapter = getPollAdapter(poll);
        if(pollAdapter != null) {
            pollAdapter.insert(poll, 0);
            pollAdapter.notifyDataSetChanged();
        }
    }

    public void addDownloadedPollsToListAndMap(List<Poll> pollList){
        for (int i = 0; i < pollList.size(); i++){
            Poll poll = pollList.get(i);
            addPollToListAndMap(poll);
        }
        openPollsAdapter.notifyDataSetChanged();
        closedPollsAdapter.notifyDataSetChanged();
    }

    public int getCombinedListSize(){
        return openPollsAdapter.list.size()+ closedPollsAdapter.list.size();
    }

    private PollAdapter getPollAdapter(Poll poll){
        switch(poll.getStatus()){
            case OPEN:
                return openPollsAdapter;
            case CLOSED:
                return closedPollsAdapter;
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

    public void removePollFromListAndMap(Poll poll){
        PollAdapter pollAdapter;
        pollAdapter = getPollAdapter(poll);
        if(pollAdapter != null) {
            removePollFromList(poll.getId(), pollAdapter.list);
            pollAdapter.notifyDataSetChanged();
            pollMap.remove(poll.getId());
        }
    }

    public void changePollStatusToClosed(Poll poll){
        removePollFromListAndMap(poll);
        poll.setStatus(ObjectStatus.CLOSED);

        //TODO: send update to server and if successful then delete poll from List and Map below

        addPollToListAndMap(poll);
    }

    public void changePollStatusToOpen(Poll poll){
        removePollFromListAndMap(poll);
        poll.setStatus(ObjectStatus.OPEN);

        //TODO: send update to server and if successful then delete poll from List and Map below

        addPollToListAndMap(poll);
    }

    public void clearPollListsAndMap(){
        if (openPollsAdapter != null) {
            openPollsAdapter.list.clear();
        }
        if (closedPollsAdapter != null) {
            closedPollsAdapter.list.clear();
        }
        if (pollMap != null) {
            pollMap.clear();
        }
    }
}
