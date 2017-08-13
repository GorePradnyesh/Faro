package com.zik.faro.frontend;

import android.content.Context;

import com.google.gson.Gson;
import com.zik.faro.data.BaseEntity;
import com.zik.faro.data.ObjectStatus;
import com.zik.faro.data.Poll;
import com.zik.faro.frontend.util.FaroObjectNotFoundException;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class PollListHandler extends BaseObjectHandler<Poll>{
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

    @Override
    public Poll getOriginalObject(String pollId) throws FaroObjectNotFoundException {
        Poll poll = pollMap.get(pollId);
        if (poll == null) {
            throw new FaroObjectNotFoundException
                    (MessageFormat.format("Poll with id {0} not found in global memory", pollId));
        } else {
            return poll;
        }
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
        removeAllPollsFromListAndMapForEvent(eventId, context);

        for (Poll poll : pollList){
            addPollToListAndMap(eventId, poll, context);
        }

        getOpenPollAdapter(eventId, context).notifyDataSetChanged();
        getClosedPollAdapter(eventId, context).notifyDataSetChanged();
    }

    public void removeAllFromListAndMap (PollAdapter pollAdapter) {
        for (Iterator<Poll> iterator = pollAdapter.list.iterator(); iterator.hasNext();){
            Poll poll = iterator.next();
            pollMap.remove(poll.getId());
            iterator.remove();
        }
        pollAdapter.notifyDataSetChanged();
    }

    public void removeAllPollsFromListAndMapForEvent (String eventId, Context context) {
        removeAllFromListAndMap(getOpenPollAdapter(eventId, context));
        removeAllFromListAndMap(getClosedPollAdapter(eventId, context));
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
        for (Iterator<Poll> iterator = pollList.iterator(); iterator.hasNext();){
            Poll poll = iterator.next();
            if (poll.getId().equals(pollId)){
                iterator.remove();
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

    @Override
    public Class<Poll> getType() {
        return Poll.class;
    }
}
