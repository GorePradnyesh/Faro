package com.zik.faro.frontend;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.okhttp.Request;
import com.zik.faro.data.Activity;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;
import com.zik.faro.frontend.notification.NotificationPayloadHandler;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class ActivityLandingPage extends android.app.Activity implements NotificationPayloadHandler {

    private DateFormat sdf = new SimpleDateFormat(" EEE, MMM d, yyyy");
    private DateFormat stf = new SimpleDateFormat("hh:mm a");
    private Activity cloneActivity = null;
    private static ActivityListHandler activityListHandler = ActivityListHandler.getInstance();
    private static FaroServiceHandler serviceHandler = FaroServiceHandler.getFaroServiceHandler();
    private String eventID = null;
    private String activityID = null;
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
    private String isNotification = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_landing_page);

        Thread.setDefaultUncaughtExceptionHandler(new FaroExceptionHandler(this));

        linlaHeaderProgress = (LinearLayout) findViewById(R.id.linlaHeaderProgress);

        activityLandingPageRelativeLayout = (RelativeLayout) findViewById(R.id.activityLandingPageRelativeLayout);
        activityLandingPageRelativeLayout.setVisibility(View.GONE);

        EditActivityPage = new Intent(ActivityLandingPage.this, EditActivity.class);
        AssignmentLandingPageIntent = new Intent(ActivityLandingPage.this, AssignmentLandingPage.class);

        checkAndHandleNotification();
    }

    private void setupPageDetails () {

        cloneActivity = activityListHandler.getActivityCloneFromMap(activityID);

        linlaHeaderProgress.setVisibility(View.GONE);
        activityLandingPageRelativeLayout.setVisibility(View.VISIBLE);

        editButton = (ImageButton)findViewById(R.id.editButton);
        editButton.setImageResource(R.drawable.edit);

        activityAssignmentButton = (ImageButton)findViewById(R.id.activityAssignmentImageButton);
        activityAssignmentButton.setImageResource(R.drawable.assignment_icon);

        activityName = (TextView) findViewById(R.id.activityNameText);
        activityName.setText(cloneActivity.getName());

        activityDescription = (TextView) findViewById(R.id.activityDescriptionTextView);
        activityDescription.setText(cloneActivity.getDescription());

        startDateAndTime = (TextView)findViewById(R.id.startDateAndTimeDisplay);
        startDateAndTime.setText(sdf.format(cloneActivity.getStartDate().getTime()) + " at " +
                stf.format(cloneActivity.getStartDate().getTime()));

        endDateAndTime = (TextView)findViewById(R.id.endDateAndTimeDisplay);
        endDateAndTime.setText(sdf.format(cloneActivity.getEndDate().getTime()) + " at " +
                stf.format(cloneActivity.getEndDate().getTime()));

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditActivityPage.putExtra("eventID", eventID);
                EditActivityPage.putExtra("activityID", activityID);
                startActivity(EditActivityPage);
                finish();
            }
        });

        activityAssignmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AssignmentLandingPageIntent.putExtra("eventID", eventID);
                AssignmentLandingPageIntent.putExtra("activityID", activityID);
                AssignmentLandingPageIntent.putExtra("assignmentID", cloneActivity.getAssignment().getId());
                startActivity(AssignmentLandingPageIntent);
            }
        });
    }

    @Override
    public void checkAndHandleNotification() {
        extras = getIntent().getExtras();
        if(extras == null) return; //TODO How to handle this case?

        eventID = extras.getString("eventID");
        activityID = extras.getString("activityID");
        isNotification = extras.getString("bundleType");

        Log.d(TAG, "******eventID is " + eventID);
        Log.d(TAG, "******activityID is " + activityID);

        if (isNotification == null){
            setupPageDetails();
        } else {
            //Else the bundleType is "notification"
            //Here we will get all activities from the server and also the assignement for the event
            // This is done to show "All My Items" in case when the user clicks the assignment
            // Button on the activity page and clicks on "My Items"
            getEventActivitiesFromServer();

            //TODO : Do getEvent call here and store the assignment from the event as well.

            //TODO: Have 2 flags for each API call and when both are true only then call setupPageDetails
        }
    }

    public void getEventActivitiesFromServer(){
        serviceHandler.getActivityHandler().getActivities(new BaseFaroRequestCallback<List<Activity>>() {
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
                            activityListHandler.addDownloadedActivitiesToListAndMap(eventID, activities, mContext);
                            setupPageDetails();
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
