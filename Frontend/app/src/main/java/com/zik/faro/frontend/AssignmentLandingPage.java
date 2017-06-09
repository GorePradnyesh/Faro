package com.zik.faro.frontend;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.zik.faro.data.Activity;
import com.zik.faro.data.Event;

public class AssignmentLandingPage extends FragmentActivity {
    private FragmentTabHost mTabHost;


    private String activityID = null;
    private String eventID = null;
    private String assignmentID = null;
    private String isNotification = null;
    private static AssignmentListHandler assignmentListHandler = AssignmentListHandler.getInstance();
    private static EventListHandler eventListHandler = EventListHandler.getInstance();
    private static ActivityListHandler activityListHandler = ActivityListHandler.getInstance();
    private Activity cloneActivity = null;
    private Event cloneEvent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment_landing_page_tabs);

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            eventID = extras.getString("eventID");
            activityID = extras.getString("activityID");
            assignmentID = extras.getString("assignmentID");
            isNotification = extras.getString("bundleType");

            if (activityID == null){
                cloneEvent = eventListHandler.getEventCloneFromMap(eventID);
            } else {
                cloneActivity = activityListHandler.getActivityCloneFromMap(activityID);
            }

            Bundle bundle = new Bundle();
            bundle.putString("eventID", eventID);
            bundle.putString("activityID", activityID);
            bundle.putString("assignmentID", assignmentID);
            bundle.putString("bundleType", isNotification);

            Thread.setDefaultUncaughtExceptionHandler(new FaroExceptionHandler(this));
            mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
            mTabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);
            mTabHost.addTab(
                    mTabHost.newTabSpec("tab1").setIndicator(getTabIndicator(mTabHost.getContext(), "All Items")),
                    AssignmentLandingFragment.class, bundle);
            mTabHost.addTab(
                    mTabHost.newTabSpec("tab2").setIndicator(getTabIndicator(mTabHost.getContext(), "My Items")),
                    MyAssignmentsFragment.class, bundle);
        }
    }

    private View getTabIndicator(Context context, String tabText) {
        View view = LayoutInflater.from(context).inflate(R.layout.assignment_landing_tab_layout, null);
        TextView tabTextView = (TextView)view.findViewById(R.id.tabTextView);
        tabTextView.setText(tabText);
        return view;
    }

    @Override
    protected void onResume() {
        if (isNotification == null) {
            // Check if the version is same. It can be different if this page is loaded and a notification
            // is received for this later which updates the global memory but clonedata on this page remains
            // stale.
            // This check is not necessary when opening this page directly through a notification.
            Long versionInGlobalMemory = null;
            Long previousVersion = null;

            if (activityID == null) {
                versionInGlobalMemory = eventListHandler.getOriginalEventFromMap(eventID).getVersion();
                previousVersion = cloneEvent.getVersion();
            } else {
                versionInGlobalMemory = activityListHandler.getOriginalActivityFromMap(activityID).getVersion();
                previousVersion = cloneActivity.getVersion();
            }

            if (!previousVersion.equals(versionInGlobalMemory)) {
                Intent assignmentLandingPageReloadIntent = new Intent(AssignmentLandingPage.this, AssignmentLandingPage.class);
                assignmentLandingPageReloadIntent.putExtra("eventID", eventID);
                assignmentLandingPageReloadIntent.putExtra("activityID", activityID);
                assignmentLandingPageReloadIntent.putExtra("assignmentID", assignmentID);
                finish();
                startActivity(assignmentLandingPageReloadIntent);
            }
        }
        super.onResume();
    }
}
