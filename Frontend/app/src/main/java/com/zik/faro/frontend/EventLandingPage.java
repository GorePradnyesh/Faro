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

import com.squareup.okhttp.Request;
import com.zik.faro.data.Event;
import com.zik.faro.data.user.EventInviteStatus;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;

public class EventLandingPage extends Activity {

    public static final int NO_CHANGES = 0;
    private DateFormat sdf = new SimpleDateFormat(" EEE, MMM d, yyyy");
    private DateFormat stf = new SimpleDateFormat("hh:mm a");
    private static EventListHandler eventListHandler = EventListHandler.getInstance();
    static PollListHandler pollListHandler = PollListHandler.getInstance();
    private static FaroServiceHandler serviceHandler = eventListHandler.serviceHandler;

    private static String TAG = "EventLandingPage";

    private static Event event;

    private Button statusYes = null;
    private Button statusNo = null;
    private Button statusMaybe = null;
    private ImageButton pollButton = null;
    private ImageButton eventAssignmentButton = null;
    private ImageButton activityButton = null;
    private ImageButton editButton = null;
    private ImageButton addFriendsButton = null;
    private TextView event_status = null;

    private String eventID;
    final Context mContext = this;
    private Intent EventLandingPageReload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_landing_page);

        final TextView event_name = (TextView) findViewById(R.id.eventNameText);
        event_status = (TextView) findViewById(R.id.eventStatusText);
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
        final Intent CreateEventAssignment = new Intent(EventLandingPage.this, CreateNewAssignment.class);
        final Intent AssignmentLandingPage = new Intent(EventLandingPage.this, AssignmentLandingPage.class);
        EventLandingPageReload = new Intent(EventLandingPage.this, EventLandingPage.class);

        Thread.setDefaultUncaughtExceptionHandler(new FaroExceptionHandler(this));

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            eventID = extras.getString("eventID");
            event = eventListHandler.getEventCloneFromMap(eventID);
            if (event != null) {

                //Display elements based on Event Status
                eventStateBasedView(event);

                controlFlagBasedView();

                String ev_name = event.getEventName();
                event_name.setText(ev_name);

                String eventDescr = event.getEventDescription();
                eventDescription.setText(eventDescr);

                startDateAndTime.setText(sdf.format(event.getStartDate().getTime()) + " at " +
                        stf.format(event.getStartDate().getTime()));

                endDateAndTime.setText(sdf.format(event.getEndDate().getTime()) + " at " +
                        stf.format(event.getEndDate().getTime()));


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
            }
        }

        pollButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PollListPage.putExtra("eventID", event.getEventId());
                startActivity(PollListPage);
            }
        });

        eventAssignmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (event.getAssignment() == null) {
                    CreateEventAssignment.putExtra("eventID", event.getEventId());
                    startActivity(CreateEventAssignment);
                    finish();
                } else {
                    AssignmentLandingPage.putExtra("eventID", event.getEventId());
                    AssignmentLandingPage.putExtra("assignmentID", event.getAssignment().getId());
                    startActivity(AssignmentLandingPage);
                    finish();
                }
            }
        });

        activityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityListPage.putExtra("eventID", event.getEventId());
                startActivity(ActivityListPage);
            }
        });

        addFriendsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InviteFriendToEventPage.putExtra("eventID", event.getEventId());
                startActivity(InviteFriendToEventPage);
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditEvent.putExtra("eventID", event.getEventId());
                startActivity(EditEvent);
                finish();
            }
        });
    }

    private void updateUserEventInviteStatus(final EventInviteStatus eventInviteStatus) {
        serviceHandler.getEventHandler().updateEventUserInviteStatus(new BaseFaroRequestCallback<String>() {
            @Override
            public void onFailure(Request request, IOException ex) {
                Log.e(TAG, "failed to send event Invite Status");
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
                                    eventListHandler.addEventToListAndMap(event, EventInviteStatus.ACCEPTED);
                                    //Reload EventLandingPage
                                    EventLandingPageReload.putExtra("eventID", event.getEventId());
                                    finish();
                                    startActivity(EventLandingPageReload);
                                    break;
                                case MAYBE:
                                    eventListHandler.addEventToListAndMap(event, EventInviteStatus.MAYBE);
                                    //Reload EventLandingPage
                                    EventLandingPageReload.putExtra("eventID", event.getEventId());
                                    finish();
                                    startActivity(EventLandingPageReload);
                                    break;
                                case DECLINED:
                                    eventListHandler.removeEventFromListAndMap(event.getEventId());
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

        EventInviteStatus inviteStatus = eventListHandler.getUserEventStatus(event.getEventId());
        switch (inviteStatus) {
            case ACCEPTED:
                event_status.setText("Accepted");
                break;
            case MAYBE:
                event_status.setText("Maybe");
                break;
            case INVITED:
                event_status.setText("Not responded");
                break;
        }
        
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
        if(event.getEventCreatorId() == null) {
            if (event.getControlFlag()) {
                editButton.setVisibility(View.GONE);
            }
        }
    }

    //Clear all event related datastructures
    @Override
    public void onBackPressed() {
        eventListHandler.deleteEventFromMapIfNotInList(event);
        pollListHandler.clearPollListsAndMap();
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
