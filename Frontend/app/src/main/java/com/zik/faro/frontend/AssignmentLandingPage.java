package com.zik.faro.frontend;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class AssignmentLandingPage extends FragmentActivity {
    private FragmentTabHost mTabHost;


    private String activityID = null;
    private String eventID = null;
    private String assignmentID = null;
    private String isNotification = null;
    private Context mContext = this;
    private static ActivityListHandler activityListHandler = ActivityListHandler.getInstance();

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
}
