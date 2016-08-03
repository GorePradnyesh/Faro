package com.zik.faro.frontend;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.squareup.okhttp.Request;
import com.zik.faro.data.AddFriendRequest;
import com.zik.faro.data.InviteeList;
import com.zik.faro.data.MinUser;
import com.zik.faro.data.user.EventInviteStatus;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;
import com.zik.faro.frontend.faroservice.auth.FaroUserContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static android.widget.Toast.LENGTH_LONG;

public class InviteFriendToEventPage extends Activity {

    private static FriendListHandler friendListHandler = FriendListHandler.getInstance();
    static EventListHandler eventListHandler = EventListHandler.getInstance();
    private static FaroServiceHandler serviceHandler = eventListHandler.serviceHandler;
    static FaroUserContext faroUserContext = FaroUserContext.getInstance();
    private List <MinUser> friendList;
    private static final Integer FRIEND_ROW_HEIGHT = 100;
    private static String TAG = "InviteFriendToEventPage";
    private String eventID;
    private Map<String, InviteeList.Invitees>originalInviteeMap = new ConcurrentHashMap<>();
    private Map<String, String>inviteNewInviteesMap = new ConcurrentHashMap<>();

    private Map<String, String>unInviteInviteesMap = new ConcurrentHashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_friend_to_event_page);

        final Context mContext = this;

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            eventID = extras.getString("eventID");
        }

        final String myUserId = faroUserContext.getEmail();

        //TODO: API call to get event Invites and store them in inviteesList
        serviceHandler.getEventHandler().getEventInvitees(new BaseFaroRequestCallback<InviteeList>() {
            @Override
            public void onFailure(Request request, IOException ex) {
                Log.e(TAG, "failed to get event Invitees");
            }

            @Override
            public void onResponse(final InviteeList inviteeList, HttpError error) {
                if (error == null ) {
                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "Successfully received Invitee List for the event");
                            originalInviteeMap = inviteeList.getUserStatusMap();

                            LinearLayout pickFriendsCheckboxList = (LinearLayout) findViewById(R.id.pickFriendsCheckboxList);
                            int friendListSize = friendListHandler.friendAdapter.getCount();
                            friendList = friendListHandler.friendAdapter.getList();
                            for (int i = 0; i < friendListSize; i++){
                                final CheckBox checkBox = new CheckBox(mContext);
                                MinUser minUser = friendList.get(i);
                                checkBox.setText(minUser.getFirstName());
                                checkBox.setId(i);
                                if (originalInviteeMap.containsKey(minUser.getEmail())){
                                    checkBox.setChecked(true);
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
                        }
                    };
                    Handler mainHandler = new Handler(mContext.getMainLooper());
                    mainHandler.post(myRunnable);
                }else {
                    Log.i(TAG, "code = " + error.getCode() + ", message = " + error.getMessage());
                }
            }
        }, eventID);



        Button updateInviteeList = (Button) findViewById(R.id.addFriends);

        updateInviteeList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inviteNewInviteesMap.isEmpty() && unInviteInviteesMap.isEmpty()){
                    Toast.makeText(InviteFriendToEventPage.this, "No change in invitee list", LENGTH_LONG).show();
                }
                if (!inviteNewInviteesMap.isEmpty()){

                    //Getting the keys i.e. the emailIDs from the map and creating a list
                    List <String>inviteNewInviteesIDList = new ArrayList<String>();
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
                                Runnable myRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.i(TAG, "Friends successfully invited to the event");
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
}
