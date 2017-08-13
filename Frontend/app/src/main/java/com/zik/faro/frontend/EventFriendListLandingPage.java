package com.zik.faro.frontend;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.zik.faro.data.Event;
import com.zik.faro.frontend.faroservice.auth.FaroUserContext;
import com.zik.faro.frontend.util.FaroIntentInfoBuilder;
import com.zik.faro.frontend.util.FaroObjectNotFoundException;

import java.text.MessageFormat;

public class EventFriendListLandingPage extends FragmentActivity {
    private FragmentTabHost mTabHost;
    private String eventId;
    private Event cloneEvent;

    private EventListHandler eventListHandler = EventListHandler.getInstance();
    private EventFriendListHandler eventFriendListHandler = EventFriendListHandler.getInstance();

    FaroUserContext faroUserContext = FaroUserContext.getInstance();
    String myUserId = faroUserContext.getEmail();

    private Context mContext;
    private String TAG = "EvntFrndListFragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_friend_list_landing_page);

        mContext = this;

        TextView guestListTextView = (TextView)findViewById(R.id.guestListTitle);
        ImageButton addFriendsImageButton = (ImageButton) findViewById(R.id.addFriendsImageButton);
        addFriendsImageButton.setImageResource(R.drawable.plus);

        final Intent InviteFriendToEventPage = new Intent(EventFriendListLandingPage.this, InviteFriendToEventPage.class);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            eventId = extras.getString(FaroIntentConstants.EVENT_ID);
            try {
                cloneEvent = eventListHandler.getCloneObject(eventId);
            } catch (FaroObjectNotFoundException e) {
                Log.i(TAG, MessageFormat.format("Event {0} has been deleted", eventId));
                finish();
            }
            if (!cloneEvent.getEventCreatorId().equals(myUserId)){
                addFriendsImageButton.setVisibility(View.GONE);
            }

        }

        addFriendsImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FaroIntentInfoBuilder.eventIntent(InviteFriendToEventPage, eventId);
                startActivity(InviteFriendToEventPage);
                finish();
            }
        });

        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);

        mTabHost.addTab(
                mTabHost.newTabSpec("tab1").setIndicator("Going" + "(" + eventFriendListHandler.getAcceptedFriendCount(eventId, mContext) + ")"),
                EventFriendListFragment.class, getBundleForListType("Going"));
        mTabHost.addTab(
                mTabHost.newTabSpec("tab2").setIndicator("Maybe" + "(" + eventFriendListHandler.getMayBeFriendCount(eventId, mContext) + ")"),
                EventFriendListFragment.class, getBundleForListType("Maybe"));
        mTabHost.addTab(
                mTabHost.newTabSpec("tab3").setIndicator("Invited" + "(" + eventFriendListHandler.getInvitedFriendCount(eventId, mContext) + ")"),
                EventFriendListFragment.class, getBundleForListType("Invited"));
        mTabHost.addTab(
                mTabHost.newTabSpec("tab4").setIndicator("Not Going" + "(" + eventFriendListHandler.getDeclinedFriendCount(eventId, mContext) + ")"),
                EventFriendListFragment.class, getBundleForListType("Not Going"));
    }

    private Bundle getBundleForListType(String listStatus){
        Bundle bundle = new Bundle();
        bundle.putString(FaroIntentConstants.EVENT_ID, eventId);
        bundle.putString(FaroIntentConstants.LIST_STATUS, listStatus);
        bundle.putString(FaroIntentConstants.BUNDLE_TYPE, FaroIntentConstants.IS_NOT_NOTIFICATION);
        return bundle;
    }
}
