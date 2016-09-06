package com.zik.faro.frontend;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import com.squareup.okhttp.Request;
import com.zik.faro.data.Event;
import com.zik.faro.data.InviteeList;
import com.zik.faro.data.user.EventInviteStatus;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;

public class EventLandingPage extends Activity {

    public static final int NO_CHANGES = 0;
    private DateFormat sdf = new SimpleDateFormat(" EEE, MMM d, yyyy");
    private DateFormat stf = new SimpleDateFormat("hh:mm a");
    private static EventListHandler eventListHandler = EventListHandler.getInstance();
    private static PollListHandler pollListHandler = PollListHandler.getInstance();
    private static ActivityListHandler activityListHandler = ActivityListHandler.getInstance();
    private static EventFriendListHandler eventFriendListHandler = EventFriendListHandler.getInstance();
    private static AssignmentListHandler assignmentListHandler = AssignmentListHandler.getInstance();

    private static FaroServiceHandler serviceHandler = eventListHandler.serviceHandler;

    private static String TAG = "EventLandingPage";

    private static Event cloneEvent;

    private Button statusYes = null;
    private Button statusNo = null;
    private Button statusMaybe = null;
    private ImageButton pollButton = null;
    private ImageButton eventAssignmentButton = null;
    private ImageButton activityButton = null;
    private ImageButton editButton = null;
    private ImageButton addFriendsButton = null;

    private String eventID;
    final Context mContext = this;
    private Intent EventLandingPageReload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_landing_page);

        final TextView event_name = (TextView) findViewById(R.id.eventNameText);
        TextView eventDescription = (TextView) findViewById(R.id.eventDescriptionTextView);

        TextView startDateAndTime = (TextView) findViewById(R.id.startDateAndTimeDisplay);

        TextView endDateAndTime = (TextView) findViewById(R.id.endDateAndTimeDisplay);

        pollButton = (ImageButton) findViewById(R.id.pollImageButton);
        pollButton.setImageResource(R.drawable.poll_icon);
        eventAssignmentButton = (ImageButton) findViewById(R.id.eventAssignmentImageButton);
        eventAssignmentButton.setImageResource(R.drawable.assignment_icon);
        activityButton = (ImageButton) findViewById(R.id.activityImageButton);
        activityButton.setImageResource(R.drawable.activity);
        addFriendsButton = (ImageButton) findViewById(R.id.addFriendsImageButton);
        addFriendsButton.setImageResource(R.drawable.friend_list);
        editButton = (ImageButton) findViewById(R.id.editButton);
        editButton.setImageResource(R.drawable.edit);


        statusYes = (Button) findViewById(R.id.statusYes);
        statusNo = (Button) findViewById(R.id.statusNo);
        statusMaybe = (Button) findViewById(R.id.statusMaybe);

        final Intent PollListPage = new Intent(EventLandingPage.this, PollListPage.class);
        final Intent EditEvent = new Intent(EventLandingPage.this, EditEvent.class);
        final Intent ActivityListPage = new Intent(EventLandingPage.this, ActivityListPage.class);
        final Intent InviteFriendToEventPage = new Intent(EventLandingPage.this, InviteFriendToEventPage.class);
        final Intent AssignmentLandingPageTabsIntent = new Intent(EventLandingPage.this, AssignmentLandingPage.class);
        EventLandingPageReload = new Intent(EventLandingPage.this, EventLandingPage.class);

        Thread.setDefaultUncaughtExceptionHandler(new FaroExceptionHandler(this));

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            eventID = extras.getString("eventID");
            cloneEvent = eventListHandler.getEventCloneFromMap(eventID);
            if (cloneEvent != null) {

                //Display elements based on Event Status
                eventStateBasedView(cloneEvent);

                controlFlagBasedView();

                String ev_name = cloneEvent.getEventName();
                event_name.setText(ev_name);

                String eventDescr = cloneEvent.getEventDescription();
                eventDescription.setText(eventDescr);

                startDateAndTime.setText(sdf.format(cloneEvent.getStartDate().getTime()) + " at " +
                        stf.format(cloneEvent.getStartDate().getTime()));

                endDateAndTime.setText(sdf.format(cloneEvent.getEndDate().getTime()) + " at " +
                        stf.format(cloneEvent.getEndDate().getTime()));


                statusYes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateUserEventInviteStatus(EventInviteStatus.ACCEPTED);
                    }
                });

                statusMaybe.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateUserEventInviteStatus(EventInviteStatus.MAYBE);
                    }
                });

                statusNo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateUserEventInviteStatus(EventInviteStatus.DECLINED);
                    }
                });

                pollButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PollListPage.putExtra("eventID", eventID);
                        startActivity(PollListPage);
                    }
                });

                eventAssignmentButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AssignmentLandingPageTabsIntent.putExtra("eventID", eventID);
                        AssignmentLandingPageTabsIntent.putExtra("assignmentID", cloneEvent.getAssignment().getId());
                        startActivity(AssignmentLandingPageTabsIntent);
                    }
                });

                activityButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ActivityListPage.putExtra("eventID", eventID);
                        startActivity(ActivityListPage);
                    }
                });

                addFriendsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        InviteFriendToEventPage.putExtra("eventID", eventID);
                        startActivity(InviteFriendToEventPage);
                        finish();
                    }
                });

                editButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditEvent.putExtra("eventID", eventID);
                        startActivity(EditEvent);
                        finish();
                    }
                });

                serviceHandler.getEventHandler().getEventInvitees(new BaseFaroRequestCallback<InviteeList>() {
                    @Override
                    public void onFailure(Request request, IOException ex) {
                        Log.e(TAG, "failed to get cloneEvent Invitees");
                    }

                    @Override
                    public void onResponse(final InviteeList inviteeList, HttpError error) {
                        if (error == null) {
                            Runnable myRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    Log.i(TAG, "Successfully received Invitee List for the cloneEvent");
                                    eventFriendListHandler.addDownloadedFriendsToListAndMap(inviteeList);
                                }
                            };
                            Handler mainHandler = new Handler(mContext.getMainLooper());
                            mainHandler.post(myRunnable);
                        } else {
                            Log.i(TAG, "code = " + error.getCode() + ", message = " + error.getMessage());
                        }
                    }
                }, eventID);

                //Add event's assignment to the Assignment Handler
                assignmentListHandler.addAssignmentToListAndMap(cloneEvent.getAssignment(), null);

                //Make API call to get all activities for this event
                serviceHandler.getActivityHandler().getActivities(new BaseFaroRequestCallback<List<com.zik.faro.data.Activity>>() {
                    @Override
                    public void onFailure(Request request, IOException ex) {
                        Log.e(TAG, "failed to get activity list");
                    }

                    @Override
                    public void onResponse(final List<com.zik.faro.data.Activity> activities, HttpError error) {
                        if (error == null) {
                            Runnable myRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    Log.i(TAG, "Successfully received activities from the server!!");
                                    activityListHandler.addDownloadedActivitiesToListAndMap(activities, eventID);
                                }
                            };
                            Handler mainHandler = new Handler(mContext.getMainLooper());
                            mainHandler.post(myRunnable);
                        } else {
                            Log.i(TAG, "code = " + error.getCode() + ", message = " + error.getMessage());
                        }

                    }
                }, eventID);
            }
        }
    }


    private void updateUserEventInviteStatus(final EventInviteStatus eventInviteStatus) {
        serviceHandler.getEventHandler().updateEventUserInviteStatus(new BaseFaroRequestCallback<String>() {
            @Override
            public void onFailure(Request request, IOException ex) {
                Log.e(TAG, "failed to send cloneEvent Invite Status");
            }

            @Override
            public void onResponse(String s, HttpError error) {
                if (error == null ) {
                    //Since update to server successful
                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            switch (eventInviteStatus){
                                case ACCEPTED:
                                    eventListHandler.addEventToListAndMap(cloneEvent, EventInviteStatus.ACCEPTED);
                                    //Reload EventLandingPage
                                    EventLandingPageReload.putExtra("eventID", eventID);
                                    finish();
                                    startActivity(EventLandingPageReload);
                                    break;
                                case MAYBE:
                                    eventListHandler.addEventToListAndMap(cloneEvent, EventInviteStatus.MAYBE);
                                    //Reload EventLandingPage
                                    EventLandingPageReload.putExtra("eventID", eventID);
                                    finish();
                                    startActivity(EventLandingPageReload);
                                    break;
                                case DECLINED:
                                    eventListHandler.removeEventFromListAndMap(eventID);
                                    finish();
                                    break;
                            }
                        }
                    };
                    Handler mainHandler = new Handler(mContext.getMainLooper());
                    mainHandler.post(myRunnable);
                } else {
                    Log.i(TAG, "code = " + error.getCode() + ", message = " + error.getMessage());
                }
            }
        }, eventID, eventInviteStatus);
    }

    private void eventStateBasedView(Event event){

        EventInviteStatus inviteStatus = eventListHandler.getUserEventStatus(eventID);
        
        if (inviteStatus == EventInviteStatus.ACCEPTED){
            statusYes.setVisibility(View.GONE);
            statusNo.setVisibility(View.GONE);
            statusMaybe.setVisibility(View.GONE);
            pollButton.setVisibility(View.VISIBLE);
            eventAssignmentButton.setVisibility(View.VISIBLE);
            addFriendsButton.setVisibility(View.VISIBLE);
            activityButton.setVisibility(View.VISIBLE);
            editButton.setVisibility(View.VISIBLE);
        }else{
            statusYes.setVisibility(View.VISIBLE);
            statusNo.setVisibility(View.VISIBLE);
            pollButton.setVisibility(View.GONE);
            eventAssignmentButton.setVisibility(View.GONE);
            addFriendsButton.setVisibility(View.GONE);
            activityButton.setVisibility(View.GONE);
            editButton.setVisibility(View.GONE);
        }

        if (inviteStatus == EventInviteStatus.INVITED) {
            statusMaybe.setVisibility(View.VISIBLE);
        } else if (inviteStatus == EventInviteStatus.MAYBE){
            statusMaybe.setVisibility(View.GONE);
        }
    }

    private void controlFlagBasedView() {
        if(cloneEvent.getEventCreatorId() == null) {
            if (cloneEvent.getControlFlag()) {
                editButton.setVisibility(View.GONE);
            }
        }
    }

    //TODO Clear all cloneEvent related datastructures
    @Override
    public void onBackPressed() {
        eventListHandler.deleteEventFromMapIfNotInList(cloneEvent);
        pollListHandler.clearPollListsAndMap();
        activityListHandler.clearActivityListAndMap();
        eventFriendListHandler.clearFriendListAndMap();
        assignmentListHandler.clearAssignmentListAndMap();
        finish();
        super.onBackPressed();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_event_landing_page, menu);
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
