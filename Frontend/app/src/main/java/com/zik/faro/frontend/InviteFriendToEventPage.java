package com.zik.faro.frontend;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import com.zik.faro.data.MinUser;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;
import com.zik.faro.frontend.util.FaroIntentInfoBuilder;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static android.widget.Toast.LENGTH_LONG;

public class InviteFriendToEventPage extends Activity {

    private UserFriendListHandler userFriendListHandler = UserFriendListHandler.getInstance();
    private FaroServiceHandler serviceHandler = FaroServiceHandler.getFaroServiceHandler();
    private EventFriendListHandler eventFriendListHandler = EventFriendListHandler.getInstance();

    private List <MinUser> friendList;
    private static final Integer FRIEND_ROW_HEIGHT = 100;
    private static String TAG = "InviteFriendToEventPage";
    private String eventId;

    private HashSet<String>invitedSet = new HashSet<>();
    private HashSet<String>unInvitedSet = new HashSet<>();

    private Intent EventFriendListLandingPageIntent = null;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_friend_to_event_page);

        Thread.setDefaultUncaughtExceptionHandler(new FaroExceptionHandler(this));

        mContext = this;

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            eventId = extras.getString(FaroIntentConstants.EVENT_ID);
        }

        EventFriendListLandingPageIntent = new Intent(InviteFriendToEventPage.this, EventFriendListLandingPage.class);

        final EditText searchFriend = (EditText)findViewById(R.id.searchFriend);
        Button updateInviteeList = (Button) findViewById(R.id.addFriends);

        friendList = new ArrayList<>(userFriendListHandler.userFriendAdapter.getMinUsers());

        initFriendList();

        updateInviteeList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (invitedSet.isEmpty() && unInvitedSet.isEmpty()){
                    Toast.makeText(InviteFriendToEventPage.this, "No change in invitee list", LENGTH_LONG).show();
                }
                if (!invitedSet.isEmpty()){

                    //Getting the keys i.e. the emailIds from the map and creating a list
                    final List <String>inviteNewInviteesIDList = new ArrayList<String>();
                    inviteNewInviteesIDList.addAll(invitedSet);

                    AddFriendRequest addFriendRequest = new AddFriendRequest();
                    addFriendRequest.setFriendIds(inviteNewInviteesIDList);

                    //API call to addFriends to the event
                    //Todo: Have the friends added, returned back so that the adapters can be updated.
                    serviceHandler.getEventHandler().addInviteesToEvent(new BaseFaroRequestCallback<String>() {
                        @Override
                        public void onFailure(Request request, IOException ex) {
                            Log.e(TAG, "Failed to add friends to the Event");
                        }

                        @Override
                        public void onResponse(String s, HttpError error) {
                            if (error == null ) {
                                Handler mainHandler = new Handler(mContext.getMainLooper());
                                mainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.i(TAG, "Friends successfully invited to the event");
                                        Toast.makeText(InviteFriendToEventPage.this, "Successfully Invited friends", LENGTH_LONG).show();
                                        FaroIntentInfoBuilder.eventIntent(EventFriendListLandingPageIntent, eventId);
                                        startActivity(EventFriendListLandingPageIntent);
                                        finish();
                                    }
                                });
                            }else {
                                Log.e(TAG, MessageFormat.format("code = {0) , message =  {1}", error.getCode(), error.getMessage()));
                            }
                        }
                    }, eventId, addFriendRequest);
                }

                //TODO implement uninvite friend
                if (!unInvitedSet.isEmpty()){
                    //API call to uninvite friends to the event
                }
            }
        });

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

    /*TODO to add alphabet based scroll bar check out the following link
    * https://www.youtube.com/watch?v=l6EAOIXy0JA
    */
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
        FaroIntentInfoBuilder.eventIntent(EventFriendListLandingPageIntent, eventId);
        startActivity(EventFriendListLandingPageIntent);
        finish();
        super.onBackPressed();
    }
}
