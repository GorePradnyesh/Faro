package com.zik.faro.frontend;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

public class AppLandingPage extends FragmentActivity{
    private FragmentTabHost mTabHost;

    /*
    fragment Activity code picked from
    https://maxalley.wordpress.com/2013/05/18/android-creating-a-tab-layout-with-fragmenttabhost-and-fragments/
    https://maxalley.wordpress.com/2014/09/08/android-styling-a-tab-layout-with-fragmenttabhost-and-fragments/
     */

    private static EventListHandler eventListHandler = EventListHandler.getInstance();
    private static UserFriendListHandler userFriendListHandler = UserFriendListHandler.getInstance();
    private static ActivityListHandler activityListHandler = ActivityListHandler.getInstance();
    private static AssignmentListHandler assignmentListHandler = AssignmentListHandler.getInstance();
    private static PollListHandler pollListHandler = PollListHandler.getInstance();
    private static EventFriendListHandler eventFriendListHandler = EventFriendListHandler.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_landing_page);

        final Intent createNewEventIntent = new Intent(this, CreateNewEvent.class);

        /*GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this *//* FragmentActivity *//*,
                        this *//* OnConnectionFailedListener *//*)
                .addApi()
                .addScope(Drive.SCOPE_FILE)
                .build();*/

        //Creating the Adapters for all
        if (eventListHandler.acceptedEventAdapter == null) {
            eventListHandler.acceptedEventAdapter = new EventAdapter(this, R.layout.event_row_style);
        }

        if (eventListHandler.notAcceptedEventAdapter == null) {
            eventListHandler.notAcceptedEventAdapter = new EventAdapter(this, R.layout.event_row_style);
        }

        if (userFriendListHandler.userFriendAdapter == null) {
            userFriendListHandler.userFriendAdapter = new UserFriendAdapter(this, R.layout.friend_row_style);
        }

        if (activityListHandler.activityAdapter == null) {
            activityListHandler.activityAdapter = new ActivityAdapter(this, R.layout.activity_row_style);
        }

        if (assignmentListHandler.assignmentAdapter == null) {
            assignmentListHandler.assignmentAdapter = new AssignmentAdapter(this, R.layout.event_row_style);
        }

        if (eventFriendListHandler.acceptedFriendAdapter == null) {
            eventFriendListHandler.acceptedFriendAdapter = new EventFriendAdapter(this, R.layout.friend_row_style);
        }

        if (eventFriendListHandler.invitedFriendAdapter == null) {
            eventFriendListHandler.invitedFriendAdapter = new EventFriendAdapter(this, R.layout.friend_row_style);
        }

        if (eventFriendListHandler.mayBeFriendAdapter == null) {
            eventFriendListHandler.mayBeFriendAdapter = new EventFriendAdapter(this, R.layout.friend_row_style);
        }

        if (eventFriendListHandler.declinedFriendAdapter == null) {
            eventFriendListHandler.declinedFriendAdapter = new EventFriendAdapter(this, R.layout.friend_row_style);
        }

        if (pollListHandler.openPollsAdapter == null) {
            pollListHandler.openPollsAdapter = new PollAdapter(this, R.layout.poll_list_page_row_style);
        }

        if (pollListHandler.closedPollsAdapter == null) {
            pollListHandler.closedPollsAdapter = new PollAdapter(this, R.layout.poll_list_page_row_style);
        }

        Thread.setDefaultUncaughtExceptionHandler(new FaroExceptionHandler(this));

        /*TODO For styling the tab layout checkout the following link
        * https://maxalley.wordpress.com/2014/09/08/android-styling-a-tab-layout-with-fragmenttabhost-and-fragments/
        */
        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);
        mTabHost.addTab(
                mTabHost.newTabSpec("tab1").setIndicator(getTabIndicator(mTabHost.getContext(), R.drawable.home)),
                EventListFragment.class, null);
        mTabHost.addTab(
                mTabHost.newTabSpec("tab2").setIndicator(getTabIndicator(mTabHost.getContext(), R.drawable.friend_list)),
                FriendListFragment.class, null);
        mTabHost.addTab(
                mTabHost.newTabSpec("tab3").setIndicator(getTabIndicator(mTabHost.getContext(), R.drawable.plus)),
                null, null);
        mTabHost.addTab(
                mTabHost.newTabSpec("tab4").setIndicator(getTabIndicator(mTabHost.getContext(), R.drawable.notification)),
                MoreOptionsPage.class, null);
        mTabHost.addTab(
                mTabHost.newTabSpec("tab5").setIndicator(getTabIndicator(mTabHost.getContext(), R.drawable.options)),
                MoreOptionsPage.class, null);
        mTabHost.getTabWidget().getChildAt(2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(createNewEventIntent);
            }
        });
    }

    private View getTabIndicator(Context context, int icon) {
        View view = LayoutInflater.from(context).inflate(R.layout.app_landing_tab_layout, null);
        ImageView iv = (ImageView) view.findViewById(R.id.imageView);
        iv.setImageResource(icon);
        return view;
    }
}
