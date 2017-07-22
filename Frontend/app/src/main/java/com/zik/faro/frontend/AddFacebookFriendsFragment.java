package com.zik.faro.frontend;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.share.model.AppInviteContent;
import com.facebook.share.widget.AppInviteDialog;
import com.google.common.collect.Lists;
import com.zik.faro.data.MinUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.MessageFormat;
import java.util.List;

/**
 * Created by gaurav on 7/15/17.
 */

public class AddFacebookFriendsFragment extends Fragment {
    private static final String TAG = "FacebookFriendsFragment";
    private Context context;
    private View fragmentView;

    // App Link for what should be opened when the recipient clicks on the install/play button on the app invite page.
    private static final String APP_LINK_URL = "https://fb.me/722765844577264";
    // A URL to an image to be used in the invite.
    private static final String APP_PREVIEW_IMAGE_URL = "";

    private TextView inviteFacebookFriends;
    private ListView facebookFriendsList;
    private FacebookFriendsListAdapter fbFriendsListAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Thread.setDefaultUncaughtExceptionHandler(new FaroExceptionHandler(getActivity()));
        fragmentView = inflater.inflate(R.layout.fragment_facebookfriends_list, container, false);
        return fragmentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = getActivity();

        inviteFacebookFriends = (TextView) getActivity().findViewById(R.id.inviteFacebookFriendsToAppButton);
        inviteFacebookFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Initiate workflow for inviting Facebook friends to Faro app");

                if (AppInviteDialog.canShow()) {
                    AppInviteContent content = new AppInviteContent.Builder()
                            .setApplinkUrl(APP_LINK_URL)
                            .setPreviewImageUrl(APP_PREVIEW_IMAGE_URL)
                            .build();
                    AppInviteDialog.show(getActivity(), content);
                } else {
                    Log.i(TAG, "Unable to show AppInvite Dialog");
                }
            }
        });

        fbFriendsListAdapter = new FacebookFriendsListAdapter(getActivity(), R.layout.friend_row_style);
        facebookFriendsList = (ListView) getActivity().findViewById(R.id.facebookFriendsList);
        facebookFriendsList.setAdapter(fbFriendsListAdapter);
        facebookFriendsList.setBackgroundColor(Color.BLACK);

        loadFacebookFriendsIntoView();
    }

    private class FacebookFriendsListAdapter extends ArrayAdapter<MinUser> {

        public FacebookFriendsListAdapter(@NonNull Context context, @LayoutRes int resource) {
            super(context, resource);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            View row = convertView;
            FbFriendHolder holder;

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = inflater.inflate(R.layout.friend_row_style, parent, false);
                holder = new FbFriendHolder(row);
                row.setTag(holder);
            } else {
                holder = (FbFriendHolder) row.getTag();
            }

            MinUser minUser = getItem(position);
            if (minUser != null) {
                if (minUser.getFirstName() != null) {
                    holder.setFriendName(minUser.getFirstName());
                } else {
                    holder.setFriendName(minUser.getEmail());
                }
            }

            // Load the user profile picture if available otherwise load the default pic
            Glide.with(getContext())
                    .load((minUser.getThumbProfileImageUrl() != null) ? minUser.getThumbProfileImageUrl() : R.drawable.user_pic)
                    .placeholder(R.drawable.user_pic)
                    .into(holder.getImageView());

            return row;
        }

    }

    private void loadFacebookFriendsIntoView() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        Log.i(TAG, MessageFormat.format("accessToken = {0}", accessToken));

        if (accessToken != null) {
            Bundle params = new Bundle();
            params.putString("fields", "email, id, first_name, last_name, picture");

            FbGraphApiService fbGraphApiService = new FbGraphApiService();
            fbGraphApiService.findFacebookFriends(new GraphRequest.Callback() {
                public void onCompleted(GraphResponse response) {
                    // handle the result
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
                                    minUser.setThumbProfileImageUrl(pictureUrl);
                                    fbFriendsList.add(minUser);
                                }

                                Handler mainHandler = new Handler(context.getMainLooper());
                                mainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        fbFriendsListAdapter.addAll(fbFriendsList);
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "Error processing response for friends list", e);
                        }
                    }
                }
            });
        }
    }

    private static class FbFriendHolder {
        private ImageView userPictureImageView;
        private TextView friendNameTextView;

        public FbFriendHolder(View rowInListView) {
            this.userPictureImageView = (ImageView)rowInListView.findViewById(R.id.userPicture);
            this.friendNameTextView = (TextView)rowInListView.findViewById(R.id.friendName);
        }

        public void setFriendName(String friendName) {
            friendNameTextView.setText(friendName);
        }

        public ImageView getImageView() {
            return userPictureImageView;
        }
    }
}
