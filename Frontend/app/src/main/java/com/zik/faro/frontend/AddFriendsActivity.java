package com.zik.faro.frontend;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.squareup.okhttp.Request;
import com.zik.faro.data.MinUser;
import com.zik.faro.frontend.data.FacebookMinUser;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;
import com.zik.faro.frontend.faroservice.auth.FaroUserContext;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

/**
 * Created by gaurav on 6/11/17.
 */

public class AddFriendsActivity extends FragmentActivity {
    private static final String TAG = "AddFriendsActivity";
    private FragmentTabHost addFriendsTabHost;
    private FloatingActionButton addByEmailButton;
    private Button addFriendsDoneButton;
    private View popupWindowParent;
    private Context context;
    private Map<String, MinUser> selectedFriends = Maps.newHashMap();
    private String currentTabId;
    private static final String CONTACTS_TAB_TAG = "Contacts";
    private static final String FACEBOOK_TAG_TAG = "Facebook";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friends);
        context = this;

        addFriendsTabHost = (FragmentTabHost) findViewById(R.id.addFriendsFragmentTabHost);
        addFriendsTabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);
        addFriendsTabHost.addTab(
                addFriendsTabHost.newTabSpec(CONTACTS_TAB_TAG).setIndicator(CONTACTS_TAB_TAG),
                AddPhoneContactsFragment.class, savedInstanceState);
        addFriendsTabHost.addTab(
                addFriendsTabHost.newTabSpec(FACEBOOK_TAG_TAG).setIndicator(FACEBOOK_TAG_TAG),
                AddFacebookFriendsFragment.class, savedInstanceState);

        currentTabId = CONTACTS_TAB_TAG;
        popupWindowParent = findViewById(R.id.addFriendsLayout);

        addByEmailButton = (FloatingActionButton) findViewById(R.id.addEmailFriendButton);
        addByEmailButton.setImageResource(R.drawable.plus);
        addByEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater layoutInflater = (LayoutInflater) context.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final ViewGroup container = (ViewGroup) layoutInflater.inflate(R.layout.invite_friend_popup, null);
                final PopupWindow popupWindow = new PopupWindow(container, RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT, true);
                final EditText emailIdEditText = (EditText) container.findViewById(R.id.friend_email_id);
                final Button sendInviteButton = (Button) container.findViewById(R.id.send_invite_button);

                popupWindow.showAtLocation(popupWindowParent, Gravity.CENTER, 0, 0);

                container.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent event) {
                        popupWindow.dismiss();
                        return false;
                    }
                });

                emailIdEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        sendInviteButton.setEnabled(!(emailIdEditText.getText().toString().trim().isEmpty()));
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });

                sendInviteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final String friendEmailId = emailIdEditText.getText().toString().toLowerCase().trim();
                        String myUserId = FaroUserContext.getInstance().getEmail();

                        if (!friendEmailId.equals(myUserId)) {
                            addFriend(friendEmailId);
                            popupWindow.dismiss();
                        } else {
                            //TODO: Add error popUp with "Cant invite self message"
                        }
                    }
                });
        }});

        addFriendsDoneButton = (Button) findViewById(R.id.addFriendsDoneButton);
        addFriendsDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (CONTACTS_TAB_TAG.equals(getCurrentTabId()) && !selectedFriends.isEmpty()) {
                    addFriends();
                }  else if (FACEBOOK_TAG_TAG.equals(getCurrentTabId()) && !selectedFriends.isEmpty()){
                    addFacebookFriends();
                }

                // Complete this Activity and go back to the previous page
                finish();
            }
        });
        addFriendsDoneButton.setVisibility(View.GONE);
    }

    public void addSelectedFriend(MinUser minUser) {
        if (minUser instanceof FacebookMinUser) {
            selectedFriends.put(((FacebookMinUser)minUser).getFacebookUserId(), minUser);
        } else {
            selectedFriends.put(minUser.getEmail(), minUser);
        }

        showAddFriendsDoneButton();
    }

    public void removeSelectedFriend(MinUser minUser) {
        if (minUser instanceof FacebookMinUser) {
            selectedFriends.remove(((FacebookMinUser)minUser).getFacebookUserId());
        } else {
            selectedFriends.remove(minUser.getEmail());
        }

        if (selectedFriends.isEmpty()) {
            showAddByEmailButton();
        }
    }

    public void removeAllSelectedFriends() {
        selectedFriends.clear();
    }

    public void setCurrentTabId(String currentTabId) {
        this.currentTabId = currentTabId;
    }

    public String getCurrentTabId() {
        return currentTabId;
    }

    public void showAddFriendsDoneButton() {
        addByEmailButton.setVisibility(View.GONE);
        addFriendsDoneButton.setVisibility(View.VISIBLE);
    }

    public void showAddByEmailButton() {
        addFriendsDoneButton.setVisibility(View.GONE);
        addByEmailButton.setVisibility(View.VISIBLE);
    }

    private void addFriends() {
        for (MinUser minUser : selectedFriends.values()) {
            final String friendEmailId = minUser.getEmail();
            String myUserId = FaroUserContext.getInstance().getEmail();

            if (friendEmailId.equals(myUserId)) {
                //TODO: Add error popUp with "Cant invite self message"
                return;
            }

            addFriend(friendEmailId);
        }
    }

    private void addFriend(final String friendEmailId) {
        FaroServiceHandler.getFaroServiceHandler().getFriendsHandler().inviteFriend(new BaseFaroRequestCallback<String>() {
            @Override
            public void onFailure(Request request, IOException ex) {
                Log.e(TAG, MessageFormat.format("failed to send friend invite request for friend {0}", friendEmailId));
            }

            @Override
            public void onResponse(String email, HttpError httpError) {
                if (httpError == null ) {
                    Handler mainHandler = new Handler(context.getMainLooper());
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, MessageFormat.format("Friend invite for {0} succeeded", friendEmailId));
                            // TODO it would be better if we return the MinUser object instead of a String
                            MinUser minUser = new MinUser().withEmail(friendEmailId);
                            UserFriendListHandler.getInstance().addFriendToListAndMap(minUser);
                        }
                    });
                } else {
                    Log.e(TAG, MessageFormat.format("Failed to invite friend {0}. code = {1}, message = {2}", friendEmailId, httpError.getCode(), httpError.getMessage()));
                }
            }
        }, friendEmailId);
    }

    private void addFacebookFriends() {
        List<String> facebookFriendIds = Lists.newArrayList();

        for (MinUser minUser : selectedFriends.values()) {
            facebookFriendIds.add(((FacebookMinUser) minUser).getFacebookUserId());
        }

        FaroServiceHandler.getFaroServiceHandler().getFriendsHandler().inviteFacebookFriends(new BaseFaroRequestCallback<List<MinUser>>() {
            @Override
            public void onFailure(Request request, IOException ex) {
                Log.e(TAG, "failed to establish friend relation with facebook friends", ex);
            }

            @Override
            public void onResponse(List<MinUser> minUsers, HttpError httpError) {
                if (httpError == null) {
                    Log.i(TAG, "successfully established friend relation with facebook friends");
                    for (MinUser fbFriends : minUsers) {
                        UserFriendListHandler.getInstance().addFriendToListAndMap(fbFriends);
                    }
                } else {
                    Log.e(TAG, MessageFormat.format("Failed to establish friend relation with facebook friends. error = {0}", httpError));
                }
            }
        }, facebookFriendIds);
    }
}
