package com.zik.faro.frontend;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.squareup.okhttp.Request;
import com.zik.faro.data.AddFriendRequest;
import com.zik.faro.data.InviteeList;
import com.zik.faro.data.MinUser;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
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

    private HashSet<String>invitedSet = new HashSet<>();
    private HashSet<String>unInvitedSet = new HashSet<>();

    private Intent EventLandingPageIntent = null;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_friend_to_event_page);

        Thread.setDefaultUncaughtExceptionHandler(new FaroExceptionHandler(this));

        mContext = this;

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            eventID = extras.getString("eventID");
        }

        EventLandingPageIntent = new Intent(InviteFriendToEventPage.this, EventLandingPage.class);

        final EditText searchFriend = (EditText)findViewById(R.id.searchFriend);
        Button updateInviteeList = (Button) findViewById(R.id.addFriends);

        friendList = new ArrayList<>(userFriendListHandler.userFriendAdapter.getList());

        initFriendList();

        updateInviteeList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (invitedSet.isEmpty() && unInvitedSet.isEmpty()){
                    Toast.makeText(InviteFriendToEventPage.this, "No change in invitee list", LENGTH_LONG).show();
                }
                if (!invitedSet.isEmpty()){

                    //Getting the keys i.e. the emailIDs from the map and creating a list
                    final List <String>inviteNewInviteesIDList = new ArrayList<String>();
                    inviteNewInviteesIDList.addAll(invitedSet);

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

                //TODO implement uninvite friend
                if (!unInvitedSet.isEmpty()){
                    //API call to uninvite friends to the event
                }
            }
        });

        //TODO Implement search feature for friends
        searchFriend.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals("")){
                    initFriendList();
                }else {
                    searchFriendInList(s.toString().toLowerCase());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void searchFriendInList(String textToSearch){

        LinearLayout pickFriendsCheckboxList = (LinearLayout) findViewById(R.id.pickFriendsCheckboxList);
        pickFriendsCheckboxList.removeAllViews();

        for (int i = 0; i < friendList.size(); i++) {

            MinUser minUser = friendList.get(i);

            if (!((minUser.getFirstName() != null && minUser.getFirstName().toLowerCase().contains(textToSearch)) ||
                    (minUser.getLastName() != null && minUser.getLastName().toLowerCase().contains(textToSearch)))){
                continue;
            }

            final CheckBox checkBox = new CheckBox(mContext);

            checkBox.setText(minUser.getFirstName());
            checkBox.setId(i);
            if (eventFriendListHandler.isFriendInvitedToEvent(minUser.getEmail())) {
                checkBox.setChecked(true);
                if (eventFriendListHandler.isFriendComingToEvent(minUser.getEmail())) {
                    checkBox.setBackgroundColor(Color.GREEN);
                }
            }

            if (invitedSet.contains(minUser.getEmail())){
                checkBox.setChecked(true);
            }

            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckBox clickedCheckBox = (CheckBox) v;
                    int id = clickedCheckBox.getId();
                    MinUser selectedMinUser = friendList.get(id);
                    if (clickedCheckBox.isChecked()) {
                        invitedSet.add(selectedMinUser.getEmail());
                        unInvitedSet.remove(selectedMinUser.getEmail());
                        Log.d(TAG, "Add Invitee to invitedSet and remove from unInvitedSet");
                    } else {
                        unInvitedSet.add(selectedMinUser.getEmail());
                        invitedSet.remove(selectedMinUser.getEmail());
                        Log.d(TAG, "Add Invitee to unInvitedSet and remove from invitedSet");
                    }
                }
            });
            //Insert checkBox into pickFriendsCheckboxList
            RelativeLayout.LayoutParams checkBoxparams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT, FRIEND_ROW_HEIGHT);
            pickFriendsCheckboxList.addView(checkBox, checkBoxparams);
        }
    }

    private void initFriendList() {

        LinearLayout pickFriendsCheckboxList = (LinearLayout) findViewById(R.id.pickFriendsCheckboxList);
        pickFriendsCheckboxList.removeAllViews();

        for (int i = 0; i < friendList.size(); i++) {
            final CheckBox checkBox = new CheckBox(mContext);
            MinUser minUser = friendList.get(i);
            checkBox.setText(minUser.getFirstName());
            checkBox.setId(i);
            if (eventFriendListHandler.isFriendInvitedToEvent(minUser.getEmail())) {
                checkBox.setChecked(true);
                if (eventFriendListHandler.isFriendComingToEvent(minUser.getEmail())) {
                    checkBox.setBackgroundColor(Color.GREEN);
                }
            }

            if (invitedSet.contains(minUser.getEmail())){
                checkBox.setChecked(true);
            }

            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckBox clickedCheckBox = (CheckBox) v;
                    int id = clickedCheckBox.getId();
                    MinUser selectedMinUser = friendList.get(id);
                    if (clickedCheckBox.isChecked()) {
                        invitedSet.add(selectedMinUser.getEmail());
                        unInvitedSet.remove(selectedMinUser.getEmail());
                        Log.d(TAG, "Add Invitee to invitedSet and remove from unInvitedSet");
                    } else {
                        unInvitedSet.add(selectedMinUser.getEmail());
                        invitedSet.remove(selectedMinUser.getEmail());
                        Log.d(TAG, "Add Invitee to unInvitedSet and remove from invitedSet");
                    }
                }
            });
            //Insert checkBox into pickFriendsCheckboxList
            RelativeLayout.LayoutParams checkBoxparams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT, FRIEND_ROW_HEIGHT);
            pickFriendsCheckboxList.addView(checkBox, checkBoxparams);
        }
    }

    @Override
    public void onBackPressed() {
        EventLandingPageIntent.putExtra("eventID", eventID);
        startActivity(EventLandingPageIntent);
        finish();
        super.onBackPressed();
    }
}
