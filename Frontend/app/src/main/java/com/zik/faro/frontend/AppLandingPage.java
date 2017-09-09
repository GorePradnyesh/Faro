package com.zik.faro.frontend;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.zik.faro.frontend.notification.FaroFirebaseInstanceIDService;

public class AppLandingPage extends FragmentActivity{
    private FragmentTabHost mTabHost;

    private static final String TAG = "AppLandingPage";
    private BroadcastReceiver broadcastReceiver;

    private PollListHandler pollListHandler = PollListHandler.getInstance();
    private ActivityListHandler activityListHandler = ActivityListHandler.getInstance();
    private AssignmentListHandler assignmentListHandler = AssignmentListHandler.getInstance();
    private EventFriendListHandler eventFriendListHandler = EventFriendListHandler.getInstance();

    /*
    fragment Activity code picked from
    https://maxalley.wordpress.com/2013/05/18/android-creating-a-tab-layout-with-fragmenttabhost-and-fragments/
    https://maxalley.wordpress.com/2014/09/08/android-styling-a-tab-layout-with-fragmenttabhost-and-fragments/
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_landing_page);


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
                mTabHost.newTabSpec("tab3").setIndicator(getTabIndicator(mTabHost.getContext(), R.drawable.notification)),
                MoreOptionsPage.class, null);
        mTabHost.addTab(
                mTabHost.newTabSpec("tab4").setIndicator(getTabIndicator(mTabHost.getContext(), R.drawable.options)),
                MoreOptionsPage.class, null);
    }

    private View getTabIndicator(Context context, int icon) {
        View view = LayoutInflater.from(context).inflate(R.layout.app_landing_tab_layout, null);
        ImageView iv = (ImageView) view.findViewById(R.id.imageView);
        iv.setImageResource(icon);
        return view;
    }

    @Override
    protected void onResume() {
        activityListHandler.clearEverything();
        assignmentListHandler.clearEverything();
        eventFriendListHandler.clearEverything();
        pollListHandler.clearEverything();
        super.onResume();
    }
}
