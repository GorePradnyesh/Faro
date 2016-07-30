package com.zik.faro.frontend;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.zik.faro.data.ObjectStatus;
import com.zik.faro.data.Poll;


public class PollListHandler {
    private static final int MAX_POLLS_PAGE_SIZE = 100;
    private static final int MAX_TOTAL_POLLS_IN_CACHE = 200;
    public static final int MAX_TOTAL_POLLS_PER_EVENT = 200;


    public PollAdapter openPollsAdapter;
    public PollAdapter closedPollsAdapter;

    //TODO Function call to remove items from the List and Map when user keeps scrolling and caches
    // lot of polls. Have a Max limit on number of polls we will cache else will use up a lot of
    // memory and battery. Use MAX_TOTAL_POLLS_IN_CACHE to set the max polls we will cache

    /*
    * Map of events needed to access events downloaded from the server in O(1) time. The Key to the
    * Map is the eventID String which returns the Event as the value
    */
    private Map<String, Poll> pollMap = new ConcurrentHashMap<>();

    public Poll getPollFromMap(String pollID){
        return this.pollMap.get(pollID);
    }

    private static int tempPollID = 0;

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

    private String getPollID(){
        String pollIDStr = Integer.toString(this.tempPollID);
        this.tempPollID++;
        return pollIDStr;
    }

    public ErrorCodes addNewPoll(Poll poll) {
        //TODO: send update to server and if successful then add poll to List and Map below and
        // update the pollID in the Poll.
        /*String pollID = getPollID();
        for (Integer i = 0; i < poll.getPollOptions().size(); i++){
            PollOption pollOption = poll.getPollOptions().get(i);
            pollOption.setId(UUID.randomUUID().toString());
        }*/

        /*if(pollID != null) {
            poll.setId(pollID);
            conditionallyAddNewPollToList(poll);
            this.pollMap.put(pollID, poll);
            return ErrorCodes.SUCCESS;
        }
        return ErrorCodes.FAILURE;*/
        conditionallyAddNewPollToList(poll);
        return ErrorCodes.SUCCESS;
    }

    public void addPollToListAndMap(Poll poll){
        removePollFromListAndMap(poll);
        conditionallyAddNewPollToList(poll);
        this.pollMap.put(poll.getId(), poll);
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
            pollListHandler.addPollToListAndMap(poll);
        }
        this.openPollsAdapter.notifyDataSetChanged();
        this.closedPollsAdapter.notifyDataSetChanged();
    }

    public int getCombinedListSize(){
        return this.openPollsAdapter.list.size()+ this.closedPollsAdapter.list.size();
    }

    private PollAdapter getPollAdapter(Poll poll){
        switch(poll.getStatus()){
            case OPEN:
                return this.openPollsAdapter;
            case CLOSED:
                return this.closedPollsAdapter;
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

        addNewPoll(poll);
    }

    public void changePollStatusToOpen(Poll poll){
        removePollFromListAndMap(poll);
        poll.setStatus(ObjectStatus.OPEN);

        //TODO: send update to server and if successful then delete poll from List and Map below

        addNewPoll(poll);
    }

    public void clearPollListsAndMap(){
        pollListHandler.openPollsAdapter.list.clear();
        pollListHandler.closedPollsAdapter.list.clear();
        pollListHandler.pollMap.clear();
    }
}
