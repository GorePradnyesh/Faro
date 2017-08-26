package com.zik.faro.frontend.ui.activities;

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

import com.squareup.okhttp.Request;
import com.zik.faro.data.MinUser;
import com.zik.faro.frontend.ui.fragments.AddFacebookFriendsFragment;
import com.zik.faro.frontend.ui.fragments.AddPhoneContactsFragment;
import com.zik.faro.frontend.R;
import com.zik.faro.frontend.handlers.UserFriendListHandler;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;
import com.zik.faro.frontend.faroservice.auth.FaroUserContext;

import java.io.IOException;
import java.text.MessageFormat;

/**
 * Created by gaurav on 6/11/17.
 */

public class AddFriendsActivity extends FragmentActivity {
    private static final String TAG = "AddFriendsActivity";
    private FragmentTabHost addFriendsTabHost;
    private FloatingActionButton addByEmailButton;
    private View popupWindowParent;
    private Context context;
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
    }

    public void showAddByEmailButton(boolean visible) {
        if (visible) {
            addByEmailButton.setVisibility(View.VISIBLE);
        } else {
            addByEmailButton.setVisibility(View.GONE);
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
}
