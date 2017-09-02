package com.zik.faro.frontend;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.zik.faro.data.Activity;
import com.zik.faro.data.Event;
import com.zik.faro.frontend.util.FaroIntentInfoBuilder;

/*
 * This is the page where all the activities are listed in chronological order.
 * From this page, we can
 *      1. Create a New Activity (Bottom right corner)
 *      2. Click on and existing activity and go to its Activity Landing Page.
*/

public class ActivityListPage extends android.app.Activity {
    private ActivityListHandler activityListHandler = ActivityListHandler.getInstance();

    private String eventId;

    private Intent activityLandingPage = null;
    private Intent createNewActivityPage = null;

    private static String TAG = "ActivityListPage";
    private Context mContext;

    private ListView activityList;
    private ImageButton addActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_list_page);

        Thread.setDefaultUncaughtExceptionHandler(new FaroExceptionHandler(this));

        mContext = this;

        Bundle extras = getIntent().getExtras();
        if (extras == null) return; //TODO: How to handle this condition?

        eventId = extras.getString(FaroIntentConstants.EVENT_ID);

        activityList  = (ListView)findViewById(R.id.activityList);
        activityList.setBackgroundColor(Color.BLACK);
        activityList.setAdapter(activityListHandler.getActivityAdapter(eventId, mContext));

        addActivity = (ImageButton)findViewById(R.id.addNewActivityButton);
        addActivity.setImageResource(R.drawable.plus);

        activityLandingPage = new Intent(ActivityListPage.this, ActivityLandingPage.class);
        createNewActivityPage = new Intent(ActivityListPage.this, CreateNewActivity.class);

        //Listener to go to the Activity Landing Page for the activity selected from the list
        activityList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Activity activity = (Activity) parent.getItemAtPosition(position);
                FaroIntentInfoBuilder.activityIntent(activityLandingPage, eventId, activity.getId());
                //TODO: check if startActivityForResult is a better way
                startActivity(activityLandingPage);
            }
        });

        activityList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(activityListHandler.getActivityListSize(eventId, mContext) > ActivityListHandler.MAX_ACTIVITIES_PAGE_SIZE) {
                    final int lastItem = firstVisibleItem + visibleItemCount;
                    if (lastItem == totalItemCount) {
                        /*TODO Make call to server to get activities after the last activity. Send the date
                        of the last activity and also send the activityId to resolve sorting conflicts.
                         */
                        Log.d("Last", "Last");
                    }
                }
            }
        });

        //TODO Implement function to refresh activity list. Watch Vipul Shah's video on youtube. The
        //link is https://www.youtube.com/watch?v=Phc9tVSG6Aw
        //Listener to go to the Create Activity page when clicked on addActivity button.
        addActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FaroIntentInfoBuilder.eventIntent(createNewActivityPage, eventId);
                startActivity(createNewActivityPage);
            }
        });
    }
}
