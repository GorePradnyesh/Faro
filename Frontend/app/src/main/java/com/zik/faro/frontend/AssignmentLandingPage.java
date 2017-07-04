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
import com.zik.faro.frontend.util.FaroIntentInfoBuilder;

public class AssignmentLandingPage extends FragmentActivity {
    private FragmentTabHost mTabHost;
    private String activityId = null;
    private String eventId = null;
    private String assignmentId = null;
    private String bundleType = null;
    private AssignmentListHandler assignmentListHandler = AssignmentListHandler.getInstance();
    private EventListHandler eventListHandler = EventListHandler.getInstance();
    private ActivityListHandler activityListHandler = ActivityListHandler.getInstance();
    private Activity cloneActivity = null;
    private Event cloneEvent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment_landing_page_tabs);

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            eventId = extras.getString(FaroIntentConstants.EVENT_ID);
            activityId = extras.getString(FaroIntentConstants.ACTIVITY_ID);
            assignmentId = extras.getString(FaroIntentConstants.ASSIGNMENT_ID);
            bundleType = extras.getString(FaroIntentConstants.BUNDLE_TYPE);

            if (activityId == null){
                cloneEvent = eventListHandler.getEventCloneFromMap(eventId);
            } else {
                cloneActivity = activityListHandler.getActivityCloneFromMap(activityId);
            }

            Bundle bundle = new Bundle();
            bundle.putString(FaroIntentConstants.EVENT_ID, eventId);
            bundle.putString(FaroIntentConstants.ACTIVITY_ID, activityId);
            bundle.putString(FaroIntentConstants.ASSIGNMENT_ID, assignmentId);
            bundle.putString(FaroIntentConstants.BUNDLE_TYPE, bundleType);

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
        if (bundleType.equals(FaroIntentConstants.IS_NOT_NOTIFICATION)) {
            // Check if the version is same. It can be different if this page is loaded and a notification
            // is received for this later which updates the global memory but clonedata on this page remains
            // stale.
            // This check is not necessary when opening this page directly through a notification.
            Long versionInGlobalMemory = null;
            Long previousVersion = null;

            if (activityId == null) {
                versionInGlobalMemory = eventListHandler.getOriginalEventFromMap(eventId).getVersion();
                previousVersion = cloneEvent.getVersion();
            } else {
                versionInGlobalMemory = activityListHandler.getOriginalActivityFromMap(activityId).getVersion();
                previousVersion = cloneActivity.getVersion();
            }

            if (!previousVersion.equals(versionInGlobalMemory)) {
                Intent assignmentLandingPageReloadIntent = new Intent(AssignmentLandingPage.this, AssignmentLandingPage.class);
                FaroIntentInfoBuilder.assignmentIntent(assignmentLandingPageReloadIntent, eventId,
                        activityId, assignmentId);
                finish();
                startActivity(assignmentLandingPageReloadIntent);
            }
        }
        super.onResume();
    }
}
