package com.zik.faro.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.zik.faro.data.Activity;
import com.zik.faro.data.Event;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class ActivityLandingPage extends ActionBarActivity {

    private DateFormat sdf = new SimpleDateFormat(" EEE, MMM d, yyyy");
    private DateFormat stf = new SimpleDateFormat("hh:mm a");
    private static Activity activity = null;
    private static Event event = null;
    private static ActivityListHandler activityListHandler = ActivityListHandler.getInstance();
    private static EventListHandler eventListHandler = EventListHandler.getInstance();

    Intent ActivityListPage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_landing_page);

        ImageButton editButton = (ImageButton)findViewById(R.id.editButton);
        editButton.setImageResource(R.drawable.edit);

        ImageButton activityAssignmentButton = (ImageButton)findViewById(R.id.activityAssignmentImageButton);
        activityAssignmentButton.setImageResource(R.drawable.assignment_icon);

        ActivityListPage = new Intent(ActivityLandingPage.this, ActivityListPage.class);
        final Intent EditActivityPage = new Intent(ActivityLandingPage.this, EditActivity.class);
        final Intent CreateActivityAssignment = new Intent(ActivityLandingPage.this, CreateNewAssignment.class);
        final Intent AssignmentLandingPage = new Intent(ActivityLandingPage.this, AssignmentLandingPage.class);

        Thread.setDefaultUncaughtExceptionHandler(new FaroExceptionHandler(this));

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            String eventID = extras.getString("eventID");
            String activityID = extras.getString("activityID");
            event = eventListHandler.getEventFromMap(eventID);
            activity = activityListHandler.getActivityFromMap(activityID);

            if (activityID != null){
                TextView activityName = (TextView) findViewById(R.id.activityNameText);
                activityName.setText(activity.getName());

                TextView activityDescription = (TextView) findViewById(R.id.activityDescriptionTextView);
                activityDescription.setText(activity.getDescription());

                TextView startDateAndTime = (TextView)findViewById(R.id.startDateAndTimeDisplay);
                startDateAndTime.setText(sdf.format(activity.getStartDate().getTime()) + " at " +
                        stf.format(activity.getStartDate().getTime()));

                TextView endDateAndTime = (TextView)findViewById(R.id.endDateAndTimeDisplay);
                endDateAndTime.setText(sdf.format(activity.getEndDate().getTime()) + " at " +
                        stf.format(activity.getEndDate().getTime()));
            }
        }

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditActivityPage.putExtra("eventID", event.getEventId());
                EditActivityPage.putExtra("activityID", activity.getId());
                startActivity(EditActivityPage);
                finish();
            }
        });

        activityAssignmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activity.getAssignment() == null) {
                    CreateActivityAssignment.putExtra("eventID", event.getEventId());
                    CreateActivityAssignment.putExtra("activityID", activity.getId());
                    startActivity(CreateActivityAssignment);
                    finish();
                }else{
                    AssignmentLandingPage.putExtra("eventID", event.getEventId());
                    AssignmentLandingPage.putExtra("activityID", activity.getId());
                    AssignmentLandingPage.putExtra("assignmentID", activity.getAssignment().getId());
                    startActivity(AssignmentLandingPage);
                    finish();
                }

            }
        });
    }

    @Override
    public void onBackPressed() {
        activityListHandler.deleteActivityFromMapIfNotInList(activity);
        ActivityListPage.putExtra("eventID", event.getEventId());
        startActivity(ActivityListPage);
        finish();
        super.onBackPressed();
    }

}
