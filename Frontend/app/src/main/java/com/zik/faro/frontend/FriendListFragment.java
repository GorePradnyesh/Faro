package com.zik.faro.frontend;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.google.common.collect.Lists;
import com.squareup.okhttp.Request;
import com.zik.faro.data.MinUser;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;
import com.zik.faro.frontend.faroservice.auth.FaroUserContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

public class FriendListFragment extends Fragment {
    private static UserFriendListHandler userFriendListHandler = UserFriendListHandler.getInstance();
    private static FaroServiceHandler serviceHandler = FaroServiceHandler.getFaroServiceHandler();

    private RelativeLayout popUpRelativeLayout;
    private FaroUserContext faroUserContext = FaroUserContext.getInstance();
    private String myUserId = faroUserContext.getEmail();

    private Context mContext;

    private static String TAG = "FriendListFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_list, container, false);

        mContext = getActivity();
        Thread.setDefaultUncaughtExceptionHandler(new FaroExceptionHandler(getActivity()));

        // Set up friends ListView
        ListView friendListView  = (ListView)view.findViewById(R.id.friendList);
        friendListView.setBackgroundColor(Color.BLACK);
        friendListView.setAdapter(userFriendListHandler.userFriendAdapter);

        friendListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Intent userProfilePageIntent = new Intent(getActivity(), UserProfilePage.class);
                MinUser minUser = (MinUser)parent.getItemAtPosition(position);
                userProfilePageIntent.putExtra("userEmailID", minUser.getEmail());
                startActivity(userProfilePageIntent);
            }
        });

        // Relaytive layout to be used to show the pop up window for adding friend
        popUpRelativeLayout = (RelativeLayout) view.findViewById(R.id.friendListFragment);

        // Setup invite friend button
        ImageButton inviteFriendButton = (ImageButton)view.findViewById(R.id.inviteFriend);
        inviteFriendButton.setImageResource(R.drawable.plus);

        inviteFriendButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                LayoutInflater layoutInflater = (LayoutInflater) getActivity().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                ViewGroup container = (ViewGroup) layoutInflater.inflate(R.layout.invite_friend_popup, null);
                final PopupWindow popupWindow = new PopupWindow(container, RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT, true);
                final EditText emailIdEditText = (EditText) container.findViewById(R.id.friend_email_id);
                final Button sendInviteButton = (Button) container.findViewById(R.id.send_invite_button);

                popupWindow.showAtLocation(popUpRelativeLayout, Gravity.CENTER, 0, 0);

                container.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
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
                    public void onClick(View v) {
                        final String friendEmailId = emailIdEditText.getText().toString().trim();

                        if (friendEmailId.equals(myUserId)) {
                            //TODO: Add error popUp with "Cant invite self message"
                            return;
                        }

                        serviceHandler.getFriendsHandler().inviteFriend(new BaseFaroRequestCallback<String>() {
                            @Override
                            public void onFailure(Request request, IOException ex) {
                                Log.e(TAG, MessageFormat.format("failed to send friend invite request for friend {0}", friendEmailId));
                            }

                            @Override
                            public void onResponse(String s, HttpError error) {
                                if (error == null ) {
                                    Handler mainHandler = new Handler(mContext.getMainLooper());
                                    mainHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Log.i(TAG, MessageFormat.format("Friend invite for {0} succeeded", friendEmailId));
                                            // TODO it would be better if we return the MinUser object instead of a String
                                            MinUser minUser = new MinUser("", "", friendEmailId);
                                            userFriendListHandler.addFriendToListAndMap(minUser);
                                            popupWindow.dismiss();
                                        }
                                    });
                                } else {
                                    Log.i(TAG, "code = " + error.getCode() + ", message = " + error.getMessage());
                                }
                            }
                        }, friendEmailId);
                    }
                });
            }
        });

        return view;
    }

    private void addFbFriendsToListView() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        Log.i(TAG, MessageFormat.format("accessToken = {0}", accessToken));

        if (accessToken != null) {
            Bundle params = new Bundle();
            params.putString("fields", "email,id,first_name,last_name,picture");

            FbGraphApiService fbGraphApiService = new FbGraphApiService();

            fbGraphApiService.findFacebookFriends(new GraphRequest.Callback() {
                public void onCompleted(GraphResponse response) {
                            /* handle the result */
                    if (response.getError() == null) {
                        JSONObject jsonObject = response.getJSONObject();
                        Log.i(TAG, MessageFormat.format("jsonObject = {0}", jsonObject));
                        try {
                            JSONArray friendsArray = jsonObject.getJSONArray("data");
                            final List<MinUser> fbFriendsList = Lists.newArrayList();
                            if (friendsArray != null) {
                                for (int i = 0; i < friendsArray.length(); i++) {
                                    JSONObject friend = friendsArray.getJSONObject(i);
                                    String firstName = friend.getString("first_name");
                                    String lastName = friend.getString("last_name");
                                    String id = friend.getString("id");
                                    JSONObject pictureData = friend.getJSONObject("picture").getJSONObject("data");
                                    String pictureUrl = pictureData.getString("url");

                                    // TODO : Exchange the fb user id with email

                                    Log.i(TAG, MessageFormat.format("friendName = {0}, last_name = {1}, id = {2}",
                                            firstName, lastName, id));

                                    MinUser minUser = new MinUser(firstName, lastName, id);
                                    fbFriendsList.add(minUser);
                                }

                                Handler mainHandler = new Handler(mContext.getMainLooper());
                                mainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        for (MinUser fbFriend : fbFriendsList) {
                                            userFriendListHandler.addFriendToListAndMap(fbFriend);
                                        }
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "Error processing response for friends list", e);
                        }
                    }
                }
            });

            GraphRequest request = new GraphRequest(AccessToken.getCurrentAccessToken(),
                    "/me/friends",
                    null,
                    HttpMethod.GET,
                    new GraphRequest.Callback() {
                        public void onCompleted(GraphResponse response) {
                            /* handle the result */
                            if (response.getError() == null) {
                                JSONObject jsonObject = response.getJSONObject();
                                Log.i(TAG, MessageFormat.format("jsonObject = {0}", jsonObject));
                                try {
                                    JSONArray friendsArray = jsonObject.getJSONArray("data");
                                    final List<MinUser> fbFriendsList = Lists.newArrayList();
                                    if (friendsArray != null) {
                                        for (int i = 0; i < friendsArray.length(); i++) {
                                            JSONObject friend = friendsArray.getJSONObject(i);
                                            String firstName = friend.getString("first_name");
                                            String lastName = friend.getString("last_name");
                                            String id = friend.getString("id");
                                            JSONObject pictureData = friend.getJSONObject("picture").getJSONObject("data");
                                            String pictureUrl = pictureData.getString("url");

                                            // TODO : Exchange the fb user id with email

                                            Log.i(TAG, MessageFormat.format("friendName = {0}, last_name = {1}, id = {2}",
                                                    firstName, lastName, id));

                                            MinUser minUser = new MinUser(firstName, lastName, id);
                                            fbFriendsList.add(minUser);
                                        }

                                        Handler mainHandler = new Handler(mContext.getMainLooper());
                                        mainHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                for (MinUser fbFriend : fbFriendsList) {
                                                    userFriendListHandler.addFriendToListAndMap(fbFriend);
                                                }
                                            }
                                        });
                                    }
                                } catch (JSONException e) {
                                    Log.e(TAG, "Error processing response for friends list", e);
                                }
                            }
                        }
                    }
            );

            request.setParameters(params);
            request.executeAsync();
        }
    }
}
