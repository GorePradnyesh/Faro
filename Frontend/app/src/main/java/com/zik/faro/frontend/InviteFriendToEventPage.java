package com.zik.faro.frontend;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.squareup.okhttp.Request;
import com.zik.faro.data.AddFriendRequest;
import com.zik.faro.data.MinUser;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static android.widget.Toast.LENGTH_LONG;

public class InviteFriendToEventPage extends Activity {

    private static UserFriendListHandler userFriendListHandler = UserFriendListHandler.getInstance();
    static EventListHandler eventListHandler = EventListHandler.getInstance();
    private static FaroServiceHandler serviceHandler = eventListHandler.serviceHandler;
    private static EventFriendListHandler eventFriendListHandler = EventFriendListHandler.getInstance();

    private List <MinUser> friendList;
    private static final Integer FRIEND_ROW_HEIGHT = 100;
    private static String TAG = "InviteFriendToEventPage";
    private String eventID;
    private Map<String, String>inviteNewInviteesMap = new ConcurrentHashMap<>();

    private Map<String, String>unInviteInviteesMap = new ConcurrentHashMap<>();

    private Intent EventLandingPageIntent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_friend_to_event_page);

        final Context mContext = this;

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            eventID = extras.getString("eventID");
        }

        EventLandingPageIntent = new Intent(InviteFriendToEventPage.this, EventLandingPage.class);

        LinearLayout pickFriendsCheckboxList = (LinearLayout) findViewById(R.id.pickFriendsCheckboxList);
        int friendListSize = userFriendListHandler.userFriendAdapter.getCount();
        friendList = userFriendListHandler.userFriendAdapter.getList();
        for (int i = 0; i < friendListSize; i++){
            final CheckBox checkBox = new CheckBox(mContext);
            MinUser minUser = friendList.get(i);
            checkBox.setText(minUser.getFirstName());
            checkBox.setId(i);
            if (eventFriendListHandler.isFriendInvitedToEvent(minUser.getEmail())){
                checkBox.setChecked(true);
                if (eventFriendListHandler.isFriendComingToEvent(minUser.getEmail())){
                    checkBox.setBackgroundColor(Color.GREEN);
                }
            }

            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckBox clickedCheckBox = (CheckBox)v;
                    int id = clickedCheckBox.getId();
                    MinUser selectedMinUser = friendList.get(id);
                    if (clickedCheckBox.isChecked()){
                        inviteNewInviteesMap.put(selectedMinUser.getEmail(), selectedMinUser.getEmail());
                        unInviteInviteesMap.remove(selectedMinUser.getEmail());
                        Log.d(TAG, "Add Invitee to inviteNewInviteesMap and remove from unInviteInviteesMap");
                    }else{
                        unInviteInviteesMap.put(selectedMinUser.getEmail(), selectedMinUser.getEmail());
                        inviteNewInviteesMap.remove(selectedMinUser.getEmail());
                        Log.d(TAG, "Add Invitee to unInviteInviteesMap and remove from inviteNewInviteesMap");
                    }
                }
            });

            //Insert checkBox into pickFriendsCheckboxList
            RelativeLayout.LayoutParams checkBoxparams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT, FRIEND_ROW_HEIGHT);
            pickFriendsCheckboxList.addView(checkBox, checkBoxparams);
        }

        Button updateInviteeList = (Button) findViewById(R.id.addFriends);

        updateInviteeList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inviteNewInviteesMap.isEmpty() && unInviteInviteesMap.isEmpty()){
                    Toast.makeText(InviteFriendToEventPage.this, "No change in invitee list", LENGTH_LONG).show();
                }
                if (!inviteNewInviteesMap.isEmpty()){

                    //Getting the keys i.e. the emailIDs from the map and creating a list
                    final List <String>inviteNewInviteesIDList = new ArrayList<String>();
                    inviteNewInviteesIDList.addAll(inviteNewInviteesMap.keySet());

                    AddFriendRequest addFriendRequest = new AddFriendRequest();
                    addFriendRequest.setFriendIds(inviteNewInviteesIDList);

                    //API call to addFriends to the event
                    serviceHandler.getEventHandler().addInviteesToEvent(new BaseFaroRequestCallback<String>() {
                        @Override
                        public void onFailure(Request request, IOException ex) {
                            Log.e(TAG, "Failed to add friends to the Event");
                        }

                        @Override
                        public void onResponse(String s, HttpError error) {
                            if (error == null ) {
                                final Runnable myRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.i(TAG, "Friends successfully invited to the event");
                                        Toast.makeText(InviteFriendToEventPage.this, "Successfully Invited friends", LENGTH_LONG).show();
                                        EventLandingPageIntent.putExtra("eventID", eventID);
                                        startActivity(EventLandingPageIntent);
                                        finish();
                                    }
                                };
                                Handler mainHandler = new Handler(mContext.getMainLooper());
                                mainHandler.post(myRunnable);
                            }else {
                                Log.i(TAG, "code = " + error.getCode() + ", message = " + error.getMessage());
                            }
                        }
                    }, eventID, addFriendRequest);
                }
                if (!unInviteInviteesMap.isEmpty()){
                    //API call to uninvite friends to the event
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        EventLandingPageIntent.putExtra("eventID", eventID);
        startActivity(EventLandingPageIntent);
        finish();
        super.onBackPressed();
    }
}
