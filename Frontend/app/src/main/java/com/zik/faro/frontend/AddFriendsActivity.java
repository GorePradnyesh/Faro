package com.zik.faro.frontend;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

/**
 * Created by gaurav on 6/11/17.
 */

public class AddFriendsActivity extends FragmentActivity {
    private static final String TAG = "AddFriendsActivity";
    private FragmentTabHost mTabHost;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friends);

        mTabHost = (FragmentTabHost) findViewById(R.id.addFriendsFragmentTabHost);
        mTabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);
        mTabHost.addTab(
                mTabHost.newTabSpec("Contacts").setIndicator("Contacts"),
                AddPhoneContactsFragment.class, savedInstanceState);
        mTabHost.addTab(
                mTabHost.newTabSpec("Facebook").setIndicator( "Facebook"),
                AddFacebookFriendsFragment.class, savedInstanceState);
    }

    private View getTabIndicator(Context context, String tabText) {
        View view = LayoutInflater.from(context).inflate(R.layout.add_friends_tab_layout, null);
        TextView tabTextView = (TextView)view.findViewById(R.id.addFriendTabTextView);
        tabTextView.setText(tabText);
        return view;
    }
}
