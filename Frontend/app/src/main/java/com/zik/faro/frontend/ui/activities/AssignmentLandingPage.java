package com.zik.faro.frontend.ui.activities;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.zik.faro.frontend.util.FaroObjectNotFoundException;

import java.text.MessageFormat;

import static android.widget.Toast.LENGTH_LONG;
import com.zik.faro.frontend.ui.fragments.AssignmentLandingFragment;
import com.zik.faro.frontend.ui.fragments.AssignmentsFragment;
import com.zik.faro.frontend.util.FaroExceptionHandler;
import com.zik.faro.frontend.FaroIntentConstants;
import com.zik.faro.frontend.R;

public class AssignmentLandingPage extends FragmentActivity {
    private FragmentTabHost mTabHost;
    private String activityId = null;
    private String eventId = null;
    private String assignmentId = null;
    private String bundleType = null;
    private String TAG = "AssignmentLandingPage";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment_landing_page_tabs);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            eventId = extras.getString(FaroIntentConstants.EVENT_ID);
            activityId = extras.getString(FaroIntentConstants.ACTIVITY_ID);
            assignmentId = extras.getString(FaroIntentConstants.ASSIGNMENT_ID);
            bundleType = extras.getString(FaroIntentConstants.BUNDLE_TYPE);

            try {
                setupPageDetails();
            } catch (FaroObjectNotFoundException e) {
                Toast.makeText(this, (activityId == null) ? "Event" : "Activity" + " has been deleted", LENGTH_LONG).show();
                Log.i(TAG, MessageFormat.format("{0} {1} has been deleted",
                        (activityId == null) ? "Event" : "Activity",
                        (activityId == null) ? eventId : activityId));
                finish();
            }
        }
    }

    private void setupPageDetails() throws FaroObjectNotFoundException {
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
                AssignmentsFragment.class, bundle);
    }

    private View getTabIndicator(Context context, String tabText) {
        View view = LayoutInflater.from(context).inflate(R.layout.assignment_landing_tab_layout, null);
        TextView tabTextView = (TextView)view.findViewById(R.id.tabTextView);
        tabTextView.setText(tabText);
        return view;
    }
}
