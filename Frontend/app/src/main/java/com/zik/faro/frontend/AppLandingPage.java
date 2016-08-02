package com.zik.faro.frontend;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class AppLandingPage extends FragmentActivity{
    private FragmentTabHost mTabHost;

    /*
    fragment Activity code picked from
    https://maxalley.wordpress.com/2013/05/18/android-creating-a-tab-layout-with-fragmenttabhost-and-fragments/
    https://maxalley.wordpress.com/2014/09/08/android-styling-a-tab-layout-with-fragmenttabhost-and-fragments/
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_landing_page);

        final Intent CreateNewEventIntent = new Intent(this, CreateNewEvent.class);


        Thread.setDefaultUncaughtExceptionHandler(new FaroExceptionHandler(this));
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
                startActivity(CreateNewEventIntent);
            }
        });


    }

    private View getTabIndicator(Context context, int icon) {
        View view = LayoutInflater.from(context).inflate(R.layout.tab_layout, null);
        ImageView iv = (ImageView) view.findViewById(R.id.imageView);
        iv.setImageResource(icon);
        return view;
    }
}
