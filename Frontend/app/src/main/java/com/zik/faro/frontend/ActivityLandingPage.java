package com.zik.faro.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.zik.faro.data.Activity;
import com.zik.faro.data.Event;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class ActivityLandingPage extends android.app.Activity {

    private DateFormat sdf = new SimpleDateFormat(" EEE, MMM d, yyyy");
    private DateFormat stf = new SimpleDateFormat("hh:mm a");
    private static Activity cloneActivity = null;
    private static Event cloneEvent = null;
    private static ActivityListHandler activityListHandler = ActivityListHandler.getInstance();
    private static EventListHandler eventListHandler = EventListHandler.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_landing_page);

        ImageButton editButton = (ImageButton)findViewById(R.id.editButton);
        editButton.setImageResource(R.drawable.edit);

        ImageButton activityAssignmentButton = (ImageButton)findViewById(R.id.activityAssignmentImageButton);
        activityAssignmentButton.setImageResource(R.drawable.assignment_icon);

        final Intent EditActivityPage = new Intent(ActivityLandingPage.this, EditActivity.class);
        final Intent AssignmentLandingPageIntent = new Intent(ActivityLandingPage.this, AssignmentLandingPage.class);

        Thread.setDefaultUncaughtExceptionHandler(new FaroExceptionHandler(this));

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            String eventID = extras.getString("eventID");
            String activityID = extras.getString("activityID");
            cloneEvent = eventListHandler.getEventCloneFromMap(eventID);
            cloneActivity = activityListHandler.getActivityCloneFromMap(activityID);

            if (activityID != null){
                TextView activityName = (TextView) findViewById(R.id.activityNameText);
                activityName.setText(cloneActivity.getName());

                TextView activityDescription = (TextView) findViewById(R.id.activityDescriptionTextView);
                activityDescription.setText(cloneActivity.getDescription());

                TextView startDateAndTime = (TextView)findViewById(R.id.startDateAndTimeDisplay);
                startDateAndTime.setText(sdf.format(cloneActivity.getStartDate().getTime()) + " at " +
                        stf.format(cloneActivity.getStartDate().getTime()));

                TextView endDateAndTime = (TextView)findViewById(R.id.endDateAndTimeDisplay);
                endDateAndTime.setText(sdf.format(cloneActivity.getEndDate().getTime()) + " at " +
                        stf.format(cloneActivity.getEndDate().getTime()));
            }
        }

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditActivityPage.putExtra("eventID", cloneEvent.getId());
                EditActivityPage.putExtra("activityID", cloneActivity.getId());
                startActivity(EditActivityPage);
                finish();
            }
        });

        activityAssignmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AssignmentLandingPageIntent.putExtra("eventID", cloneEvent.getId());
                AssignmentLandingPageIntent.putExtra("activityID", cloneActivity.getId());
                AssignmentLandingPageIntent.putExtra("assignmentID", cloneActivity.getAssignment().getId());
                startActivity(AssignmentLandingPageIntent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        activityListHandler.deleteActivityFromMapIfNotInList(cloneActivity);
        //TODO: Probably will need to clear the AssignmentList and Map here for the cloneActivity's assignments
        finish();
        super.onBackPressed();
    }

}
