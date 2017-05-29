package com.zik.faro.frontend;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TabHost;

import com.squareup.okhttp.Request;
import com.zik.faro.data.Event;
import com.zik.faro.data.EventInviteStatusWrapper;
import com.zik.faro.data.MinUser;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;

import java.io.IOException;
import java.util.List;

public class EventListFragment extends Fragment{


    static EventListHandler eventListHandler = EventListHandler.getInstance();
    private static UserFriendListHandler userFriendListHandler = UserFriendListHandler.getInstance();
    private static FaroServiceHandler serviceHandler = FaroServiceHandler.getFaroServiceHandler();
    private static String TAG = "EventListFragment";

    private boolean receivedEvents = false;
    private boolean receivedFriends = false;

    private RelativeLayout eventListFragmentRelativeLayout = null;
    private LinearLayout linlaHeaderProgress = null;

    private Context mContext = null;

    private View fragmentView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void eventSelectedFromList(AdapterView<?> parent, int position, Intent eventLanding) {
        Event event = (Event) parent.getItemAtPosition(position);
        eventLanding.putExtra("eventID", event.getId());
        //TODO: check if startActivityForResult is a better way
        startActivity(eventLanding);
    }

    private void setupPageDetails(View view) {

        //Setup the page only after response for both eventlist and friendlist is received.
        if (!(receivedFriends && receivedEvents)) return;

        linlaHeaderProgress.setVisibility(View.GONE);
        eventListFragmentRelativeLayout.setVisibility(View.VISIBLE);

        TabHost eventTabHost = (TabHost)view.findViewById(R.id.eventTabHost);
        eventTabHost.setup();

        TabHost.TabSpec acceptedTabSpec = eventTabHost.newTabSpec("accepted");
        acceptedTabSpec.setContent(R.id.acceptedTab);
        acceptedTabSpec.setIndicator("Accepted");
        eventTabHost.addTab(acceptedTabSpec);

        TabHost.TabSpec notAcceptedTabSpec = eventTabHost.newTabSpec("not_accepted");
        notAcceptedTabSpec.setContent(R.id.notAcceptedTab);
        notAcceptedTabSpec.setIndicator("Not Accepted");
        eventTabHost.addTab(notAcceptedTabSpec);

        ListView theAcceptedListView  = (ListView)view.findViewById(R.id.acceptedList);
        theAcceptedListView.setBackgroundColor(Color.BLACK);
        theAcceptedListView.setAdapter(eventListHandler.acceptedEventAdapter);

        ListView theNotAcceptedListView  = (ListView)view.findViewById(R.id.notAcceptedList);
        theNotAcceptedListView.setBackgroundColor(Color.BLACK);
        theNotAcceptedListView.setAdapter(eventListHandler.notAcceptedEventAdapter);

        //ImageButton calendar_view = (ImageButton)view.findViewById(R.id.calendarViewButton);
        //calendar_view.setImageResource(R.drawable.calendar);

        final Intent eventLanding = new Intent(getActivity(), EventLandingPage.class);



        //Listener to go to the Event Landing Page for the event selected in the "Accepted" list
        theAcceptedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                eventSelectedFromList(parent, position, eventLanding);
            }
        });

        //Listener to go to the Event Landing Page for the event selected in the "Not Accepted" list
        theNotAcceptedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                eventSelectedFromList(parent, position, eventLanding);
            }
        });

        theAcceptedListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(eventListHandler.getAcceptedEventListSize() > EventListHandler.MAX_EVENTS_PAGE_SIZE) {
                    final int lastItem = firstVisibleItem + visibleItemCount;
                    if (lastItem == totalItemCount) {
                        /*TODO Make call to server to get events after the last event. Send the date
                        of the last event and also send the eventID to resolve sorting conflicts.
                         */
                        Log.d("Last", "Last");
                    }
                }
            }
        });

        //TODO Implement function to refresh event list. Watch Vipul Shah's video on youtube. The
        //link is https://www.youtube.com/watch?v=Phc9tVSG6Aw

        //Listener to change the view from List View to Calendar View
        /*calendar_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(eventCalendarView);
                //finish();
            }
        });*/
    }

    private void getEventsFromServer() {
        //Make API calls to get events and also friend List for the user when he first logs in to set up the user completely
        serviceHandler.getEventHandler().getEvents(new BaseFaroRequestCallback<List<EventInviteStatusWrapper>>() {
            @Override
            public void onFailure(Request request, IOException ex) {
                Log.e(TAG, "failed to get event list");
            }
            @Override
            public void onResponse(final List<EventInviteStatusWrapper> eventInviteStatusWrappers, HttpError error) {
                if (error == null ) {
                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "Successfully received events from the server!!");
                            eventListHandler.addDownloadedEventsToListAndMap(eventInviteStatusWrappers);
                            receivedEvents = true;
                            setupPageDetails(fragmentView);
                        }
                    };
                    Handler mainHandler = new Handler(mContext.getMainLooper());
                    mainHandler.post(myRunnable);
                }else {
                    Log.i(TAG, "code = " + error.getCode() + ", message = " + error.getMessage());
                }
            }
        });
    }

    public void getFriendsFromServer() {
        serviceHandler.getFriendsHandler().getFriends(new BaseFaroRequestCallback<List<MinUser>>() {
            @Override
            public void onFailure(Request request, IOException ex) {
                Log.e(TAG, "failed to get friend list");
            }

            @Override
            public void onResponse(final List<MinUser> minUsers, HttpError error) {
                if (error == null ) {
                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "Successfully received friends from the server!!");
                            userFriendListHandler.addDownloadedFriendsToListAndMap(minUsers);
                            receivedFriends = true;
                            setupPageDetails(fragmentView);
                        }
                    };
                    Handler mainHandler = new Handler(mContext.getMainLooper());
                    mainHandler.post(myRunnable);
                }else {
                    Log.i(TAG, "code = " + error.getCode() + ", message = " + error.getMessage());
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Thread.setDefaultUncaughtExceptionHandler(new FaroExceptionHandler(getActivity()));

        receivedEvents = false;
        receivedFriends = false;

        fragmentView = inflater.inflate(R.layout.fragment_event_list, container, false);

        mContext = this.getActivity();

        linlaHeaderProgress = (LinearLayout)fragmentView.findViewById(R.id.linlaHeaderProgress);
        eventListFragmentRelativeLayout = (RelativeLayout) fragmentView.findViewById(R.id.eventListFragmentRelativeLayout);
        eventListFragmentRelativeLayout.setVisibility(View.GONE);

        getEventsFromServer();

        //Let this api call be on EventListFragment and not on FriendListFragment since this is where the
        // global memory for friendlist is populated which is later used when inviting friends for
        // events. If the user never goes to the FriendListFragment then this global memory would not be
        // populated.
        getFriendsFromServer();

        return fragmentView;
    }
}
