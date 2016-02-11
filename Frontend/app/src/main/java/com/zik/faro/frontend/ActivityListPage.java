package com.zik.faro.frontend;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;

import com.zik.faro.data.Activity;
import com.zik.faro.data.Event;

/*
 * This is the page where all the activities are listed in chronological order.
 * From this page, we can
 *      1. Create a New Activity (Bottom right corner)
 *      2. Click on and existing activity and go to its Activity Landing Page.
*/

public class ActivityListPage extends android.app.Activity {

    private static Event event;
    private  static EventListHandler eventListHandler = EventListHandler.getInstance();

    static ActivityListHandler activityListHandler = ActivityListHandler.getInstance();

    Intent eventLandingPage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_list_page);

        ListView activityList  = (ListView)findViewById(R.id.activityList);
        activityList.setBackgroundColor(Color.BLACK);
        if (activityListHandler.activityAdapter == null) {
            activityListHandler.activityAdapter = new ActivityAdapter(this, R.layout.activity_row_style);
        }
        activityList.setAdapter(activityListHandler.activityAdapter);

        ImageButton addActivity = (ImageButton)findViewById(R.id.addNewActivityButton);
        addActivity.setImageResource(R.drawable.plus);

        final Intent activityLandingPage = new Intent(ActivityListPage.this, ActivityLandingPage.class);
        final Intent createNewActivityPage = new Intent(ActivityListPage.this, CreateNewActivity.class);
        eventLandingPage = new Intent(ActivityListPage.this, EventLandingPage.class);

        Thread.setDefaultUncaughtExceptionHandler(new FaroExceptionHandler(this));

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            String eventID = extras.getString("eventID");
            event = eventListHandler.getEventFromMap(eventID);
        }

        //Listener to go to the Activity Landing Page for the activity selected from the list
        activityList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Activity activity = (Activity) parent.getItemAtPosition(position);
                activityLandingPage.putExtra("activityID", activity.getId());
                activityLandingPage.putExtra("eventID", event.getEventId());
                //TODO: check if startActivityForResult is a better way
                startActivity(activityLandingPage);
                finish();
            }
        });

        activityList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(activityListHandler.getActivityListSize() > ActivityListHandler.MAX_ACTIVITIES_PAGE_SIZE) {
                    final int lastItem = firstVisibleItem + visibleItemCount;
                    if (lastItem == totalItemCount) {
                        /*TODO Make call to server to get activities after the last activity. Send the date
                        of the last activity and also send the activityID to resolve sorting conflicts.
                         */
                        Log.d("Last", "Last");
                    }
                }
            }
        });

        //TODO Implement function to refresh activity list. Watch Vipul Shah's video on youtube. The
        //link is https://www.youtube.com/watch?v=Phc9tVSG6Aw
        //Listener to go to the Create Event page when clicked on add_event button.
        addActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewActivityPage.putExtra("eventID", event.getEventId());
                startActivity(createNewActivityPage);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        eventLandingPage.putExtra("eventID", event.getEventId());
        startActivity(eventLandingPage);
        finish();
        super.onBackPressed();
    }

}
