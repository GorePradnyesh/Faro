package com.zik.faro.frontend;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.Request;
import com.zik.faro.data.Activity;
import com.zik.faro.data.Event;
import com.zik.faro.data.EventInviteStatusWrapper;
import com.zik.faro.data.InviteeList;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;
import com.zik.faro.frontend.notification.NotificationPayloadHandler;
import com.zik.faro.frontend.util.FaroIntentInfoBuilder;
import com.zik.faro.frontend.util.FaroObjectNotFoundException;

import java.io.IOException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import static android.widget.Toast.LENGTH_LONG;

public class ActivityLandingPage extends android.app.Activity implements NotificationPayloadHandler {

    private DateFormat sdf = new SimpleDateFormat(" EEE, MMM d, yyyy");
    private DateFormat stf = new SimpleDateFormat("hh:mm a");
    private Activity cloneActivity = null;
    private ActivityListHandler activityListHandler = ActivityListHandler.getInstance();
    private FaroServiceHandler serviceHandler = FaroServiceHandler.getFaroServiceHandler();
    private AssignmentListHandler assignmentListHandler = AssignmentListHandler.getInstance();
    private EventFriendListHandler eventFriendListHandler = EventFriendListHandler.getInstance();
    private EventListHandler eventListHandler = EventListHandler.getInstance();
    private String eventId = null;
    private String activityId = null;
    private ImageButton editButton = null;
    private ImageButton activityAssignmentButton = null;
    private Intent EditActivityPage = null;
    private Intent AssignmentLandingPageIntent = null;
    private Bundle extras = null;
    private TextView activityName = null;
    private TextView activityDescription = null;
    private TextView startDateAndTime = null;
    private TextView endDateAndTime = null;
    private String TAG = "ActivityLandingPage";
    private LinearLayout linlaHeaderProgress = null;
    private RelativeLayout activityLandingPageRelativeLayout = null;
    private Context mContext = this;
    private String bundleType = null;

    private boolean receivedEvent = false;
    private boolean receivedAllActivities = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_landing_page);

        mContext = this;

        Thread.setDefaultUncaughtExceptionHandler(new FaroExceptionHandler(this));

        linlaHeaderProgress = (LinearLayout) findViewById(R.id.linlaHeaderProgress);

        activityLandingPageRelativeLayout = (RelativeLayout) findViewById(R.id.activityLandingPageRelativeLayout);
        activityLandingPageRelativeLayout.setVisibility(View.GONE);

        EditActivityPage = new Intent(ActivityLandingPage.this, EditActivity.class);
        AssignmentLandingPageIntent = new Intent(ActivityLandingPage.this, AssignmentLandingPage.class);

        try {
            checkAndHandleNotification();
        } catch (FaroObjectNotFoundException e) {
            Log.e(TAG, MessageFormat.format("Activity {0} has been deleted", activityId));
            Toast.makeText(mContext, "Activity has been deleted", LENGTH_LONG).show();
            finish();
        }
    }

    private void setupPageDetails() throws FaroObjectNotFoundException{

        if (!receivedEvent || !receivedAllActivities)
            return;

        cloneActivity = activityListHandler.getCloneObject(activityId);

        linlaHeaderProgress.setVisibility(View.GONE);
        activityLandingPageRelativeLayout.setVisibility(View.VISIBLE);

        editButton = (ImageButton) findViewById(R.id.editButton);
        editButton.setImageResource(R.drawable.edit);

        activityAssignmentButton = (ImageButton) findViewById(R.id.activityAssignmentImageButton);
        activityAssignmentButton.setImageResource(R.drawable.assignment_icon);

        activityName = (TextView) findViewById(R.id.activityNameText);
        activityName.setText(cloneActivity.getName());

        activityDescription = (TextView) findViewById(R.id.activityDescriptionTextView);
        activityDescription.setText(cloneActivity.getDescription());

        startDateAndTime = (TextView) findViewById(R.id.startDateAndTimeDisplay);
        startDateAndTime.setText(sdf.format(cloneActivity.getStartDate().getTime()) + " at " +
                stf.format(cloneActivity.getStartDate().getTime()));

        endDateAndTime = (TextView) findViewById(R.id.endDateAndTimeDisplay);
        endDateAndTime.setText(sdf.format(cloneActivity.getEndDate().getTime()) + " at " +
                stf.format(cloneActivity.getEndDate().getTime()));

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FaroIntentInfoBuilder.activityIntent(EditActivityPage, eventId, activityId);
                startActivity(EditActivityPage);
                finish();
            }
        });

        activityAssignmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FaroIntentInfoBuilder.assignmentIntent(AssignmentLandingPageIntent, eventId,
                        activityId, cloneActivity.getAssignment().getId());
                startActivity(AssignmentLandingPageIntent);
            }
        });

    }

    @Override
    public void checkAndHandleNotification() throws FaroObjectNotFoundException{
        extras = getIntent().getExtras();
        if (extras == null) return; //TODO How to handle this case?

        eventId = extras.getString(FaroIntentConstants.EVENT_ID);
        activityId = extras.getString(FaroIntentConstants.ACTIVITY_ID);
        bundleType = extras.getString(FaroIntentConstants.BUNDLE_TYPE);

        Log.d(TAG, "******eventId is " + eventId);
        Log.d(TAG, "******activityId is " + activityId);

        if (bundleType.equals(FaroIntentConstants.IS_NOT_NOTIFICATION)) {
            receivedAllActivities = true;
            receivedEvent = true;
            setupPageDetails();
        } else {
            //Else the bundleType is "notification"
            //Here we will get all activities from the server and also the assignement for the event
            // This is done to show "All My Items" in case when the user clicks the assignment
            // Button on the activity page and clicks on "My Items"
            getEventActivitiesFromServer();

            getEventFromServer();

            //Make API call to get all all invitees for this event
            getEventInviteesFromServer();
        }
    }

    public void getEventActivitiesFromServer() {
        serviceHandler.getActivityHandler().getActivities(new BaseFaroRequestCallback<List<Activity>>() {
            @Override
            public void onFailure(Request request, IOException ex) {
                Log.e(TAG, "failed to get activity list");
            }

            @Override
            public void onResponse(final List<com.zik.faro.data.Activity> activities, HttpError error) {
                if (error == null) {
                    Handler mainHandler = new Handler(mContext.getMainLooper());
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            receivedAllActivities = true;
                            Log.i(TAG, "Successfully received activities from the server!!");
                            activityListHandler.addDownloadedActivitiesToListAndMap(eventId, activities, mContext);
                            try {
                                setupPageDetails();
                            } catch (FaroObjectNotFoundException e) {
                                Log.e(TAG, MessageFormat.format("Activity {0} has been deleted", activityId));
                                Toast.makeText(mContext, "Activity has been deleted", LENGTH_LONG).show();
                                finish();
                            }
                        }
                    });
                } else {
                    Log.e(TAG, MessageFormat.format("code = {0) , message =  {1}", error.getCode(), error.getMessage()));
                }
            }
        }, eventId);
    }

    public void getEventFromServer() {
        serviceHandler.getEventHandler().getEvent(new BaseFaroRequestCallback<EventInviteStatusWrapper>() {
            @Override
            public void onFailure(Request request, IOException ex) {
                Log.e(TAG, "failed to get Event from server");
            }

            @Override
            public void onResponse(final EventInviteStatusWrapper eventInviteStatusWrapper, HttpError error) {
                if (error == null) {
                    Handler mainHandler = new Handler(mContext.getMainLooper());
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "Successfully received Event from server");
                            receivedEvent = true;
                            Event event = eventInviteStatusWrapper.getEvent();
                            eventListHandler.addEventToListAndMap(event,
                                    eventInviteStatusWrapper.getInviteStatus());
                            assignmentListHandler.addAssignmentToListAndMap(eventId, event.getAssignment(), null, mContext);
                            try {
                                setupPageDetails();
                            } catch (FaroObjectNotFoundException e) {
                                Log.e(TAG, MessageFormat.format("Activity {0} has been deleted", activityId));
                                Toast.makeText(mContext, "Activity has been deleted", LENGTH_LONG).show();
                                finish();
                            }
                        }});
                } else {
                    Log.e(TAG, MessageFormat.format("code = {0) , message =  {1}", error.getCode(), error.getMessage()));
                }
            }
        }, eventId);
    }

    public void getEventInviteesFromServer(){
        serviceHandler.getEventHandler().getEventInvitees(new BaseFaroRequestCallback<InviteeList>() {
            @Override
            public void onFailure(Request request, IOException ex) {
                Log.e(TAG, "failed to get cloneEvent Invitees");
            }

            @Override
            public void onResponse(final InviteeList inviteeList, HttpError error) {
                if (error == null) {
                    Handler mainHandler = new Handler(mContext.getMainLooper());
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "Successfully received Invitee List for the cloneEvent");
                            eventFriendListHandler.addDownloadedFriendsToListAndMap(eventId, inviteeList, mContext);
                        }
                    });
                } else {
                    Log.e(TAG, MessageFormat.format("code = {0) , message =  {1}", error.getCode(), error.getMessage()));
                }
            }
        }, eventId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bundleType.equals(FaroIntentConstants.IS_NOT_NOTIFICATION)) {
            // Check if the version is same. It can be different if this page is loaded and a notification
            // is received for this later which updates the cache but clonedata on this page remains
            // stale.
            // This check is not necessary when opening this page directly through a notification.
            try {
                if (!activityListHandler.checkObjectVersionIfLatest(activityId, cloneActivity.getVersion())) {
                    setupPageDetails();
                }
            } catch (FaroObjectNotFoundException e) {
                //Activity has been deleted.
                Log.e(TAG, MessageFormat.format("Activity {0} has been deleted", activityId));
                Toast.makeText(this, "Activity has been deleted", LENGTH_LONG).show();
                finish();
            }
        }
    }
}
