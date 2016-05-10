package com.zik.faro.frontend;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TabHost;

import com.squareup.okhttp.Request;
import com.zik.faro.data.Event;
import com.zik.faro.data.EventInviteStatusWrapper;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;
import com.zik.faro.frontend.faroservice.auth.TokenCache;

import java.io.IOException;
import java.util.List;

/*
 * This is the page where all the events are listed in separate tabs based on invitation "Accepted"
 * or not.
 * Within the Accepted group, the events could have some pending stuff like unfinished
 * assignment, pending poll, etc, which needs to be represented differently.
 * Within the Not Accepted group, 2 types of events are present. One of them is  for which response
 * to the invitation is pending and the other is for which the response is Maybe. These should be
 * represented differently.
 * From this page, we can
 *      1. Create a New Event (Bottom right corner)
 *      2. Click on and existing event and go to its Event Landing Page.
 *      3. Change the view to Calendar View. (Bottom left corner)
 *      4. Check the latest chats for the events. (Top right corner)
 *      5. See the App related options like Settings, Profile page, etc (Top Left corner)
*/

public class EventListPage extends Activity{

    static EventListHandler eventListHandler = EventListHandler.getInstance();
    private static FaroServiceHandler serviceHandler = eventListHandler.serviceHandler;
    private static String TAG = "EventListPage";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list_page);

        TabHost eventTabHost = (TabHost)findViewById(R.id.eventTabHost);
        eventTabHost.setup();

        TabHost.TabSpec acceptedTabSpec = eventTabHost.newTabSpec("accepted");
        acceptedTabSpec.setContent(R.id.acceptedTab);
        acceptedTabSpec.setIndicator("Accepted");
        eventTabHost.addTab(acceptedTabSpec);

        TabHost.TabSpec notAcceptedTabSpec = eventTabHost.newTabSpec("not_accepted");
        notAcceptedTabSpec.setContent(R.id.notAcceptedTab);
        notAcceptedTabSpec.setIndicator("Not Accepted");
        eventTabHost.addTab(notAcceptedTabSpec);

        ListView theAcceptedListView  = (ListView)findViewById(R.id.acceptedList);
        theAcceptedListView.setBackgroundColor(Color.BLACK);
        if (eventListHandler.acceptedEventAdapter == null) {
            eventListHandler.acceptedEventAdapter = new EventAdapter(this, R.layout.event_row_style);
        }
        theAcceptedListView.setAdapter(eventListHandler.acceptedEventAdapter);

        ListView theNotAcceptedListView  = (ListView)findViewById(R.id.notAcceptedList);
        theNotAcceptedListView.setBackgroundColor(Color.BLACK);
        if (eventListHandler.notAcceptedEventAdapter == null) {
            eventListHandler.notAcceptedEventAdapter = new EventAdapter(this, R.layout.event_row_style);
        }
        theNotAcceptedListView.setAdapter(eventListHandler.notAcceptedEventAdapter);

        ImageButton add_event = (ImageButton)findViewById(R.id.addNewEventButton);
        add_event.setImageResource(R.drawable.plus);
        ImageButton calendar_view = (ImageButton)findViewById(R.id.calendarViewButton);
        calendar_view.setImageResource(R.drawable.calendar);
        Button logout = (Button)findViewById(R.id.logout);

        final Intent eventLanding = new Intent(EventListPage.this, EventLandingPage.class);
        final Intent create_event_page = new Intent(EventListPage.this, CreateNewEvent.class);
        final Intent eventCalendarView = new Intent(EventListPage.this, EventCalendarView.class);

        final Context mContext = this.getApplicationContext();

        Thread.setDefaultUncaughtExceptionHandler(new FaroExceptionHandler(this));

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

        //Listener to go to the Create Event page when clicked on add_event button.
        add_event.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(create_event_page);
                finish();
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TokenCache.getTokenCache().deleteToken();
                finish();
            }
        });

        //Listener to change the view from List View to Calendar View
        calendar_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(eventCalendarView);
                //finish();
            }
        });

        /*serviceHandler.getEventHandler().getEvents(new BaseFaroRequestCallback<List<EventInviteStatusWrapper>>() {
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
                            addDownloadedEventsToListAndMap(eventInviteStatusWrappers);
                        }
                    };
                    Handler mainHandler = new Handler(mContext.getMainLooper());
                    mainHandler.post(myRunnable);
                }else {
                    Log.i(TAG, "code = " + error.getCode() + ", message = " + error.getMessage());
                }
            }
        });*/
    }

    private void eventSelectedFromList(AdapterView<?> parent, int position, Intent eventLanding) {
        Event E = (Event) parent.getItemAtPosition(position);
        eventLanding.putExtra("eventID", E.getEventId());
        //TODO: check if startActivityForResult is a better way
        startActivity(eventLanding);
        finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_app_landing_page, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
