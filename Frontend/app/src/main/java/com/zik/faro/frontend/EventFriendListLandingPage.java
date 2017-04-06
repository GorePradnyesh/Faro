package com.zik.faro.frontend;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.zik.faro.data.Event;
import com.zik.faro.frontend.faroservice.auth.FaroUserContext;

public class EventFriendListLandingPage extends FragmentActivity {
    private FragmentTabHost mTabHost;
    private String eventID;
    private static Event cloneEvent;

    private static EventListHandler eventListHandler = EventListHandler.getInstance();
    private static EventFriendListHandler eventFriendListHandler = EventFriendListHandler.getInstance();

    private Intent EventLandingPageIntent = null;

    FaroUserContext faroUserContext = FaroUserContext.getInstance();
    String myUserId = faroUserContext.getEmail();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_friend_list_landing_page);

        TextView guestListTextView = (TextView)findViewById(R.id.guestListTitle);
        ImageButton addFriendsImageButton = (ImageButton) findViewById(R.id.addFriendsImageButton);
        addFriendsImageButton.setImageResource(R.drawable.plus);


        final Intent InviteFriendToEventPage = new Intent(EventFriendListLandingPage.this, InviteFriendToEventPage.class);
        EventLandingPageIntent = new Intent(EventFriendListLandingPage.this, EventLandingPage.class);


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            eventID = extras.getString("eventID");
            cloneEvent = eventListHandler.getEventCloneFromMap(eventID);
            if (!cloneEvent.getEventCreatorId().equals(myUserId)){
                addFriendsImageButton.setVisibility(View.GONE);
            }

        }

        addFriendsImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InviteFriendToEventPage.putExtra("eventID", eventID);
                startActivity(InviteFriendToEventPage);
                finish();
            }
        });

        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);

        mTabHost.addTab(
                mTabHost.newTabSpec("tab1").setIndicator("Going" + "(" + eventFriendListHandler.getAcceptedFriendCount() + ")"),
                EventFriendListFragment.class, getBundleForListType("Going"));
        mTabHost.addTab(
                mTabHost.newTabSpec("tab2").setIndicator("Maybe" + "(" + eventFriendListHandler.getMayBeFriendCount() + ")"),
                EventFriendListFragment.class, getBundleForListType("Maybe"));
        mTabHost.addTab(
                mTabHost.newTabSpec("tab3").setIndicator("Invited" + "(" + eventFriendListHandler.getInvitedFriendCount() + ")"),
                EventFriendListFragment.class, getBundleForListType("Invited"));
        mTabHost.addTab(
                mTabHost.newTabSpec("tab4").setIndicator("Not Going" + "(" + eventFriendListHandler.getDeclinedFriendCount() + ")"),
                EventFriendListFragment.class, getBundleForListType("Not Going"));
    }

    private Bundle getBundleForListType(String listType){
        Bundle bundle = new Bundle();
        bundle.putString("listType", listType);
        return bundle;
    }

    @Override
    public void onBackPressed() {
        EventLandingPageIntent.putExtra("eventID", eventID);
        startActivity(EventLandingPageIntent);
        finish();
        super.onBackPressed();
    }
}
