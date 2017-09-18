package com.zik.faro.frontend.ui.fragments;

import android.app.Activity;
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
import android.widget.Button;
import android.widget.CheckBox;
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
import com.google.common.collect.Maps;
import com.squareup.okhttp.Request;
import com.zik.faro.data.MinUser;
import com.zik.faro.frontend.util.FaroExceptionHandler;
import com.zik.faro.frontend.faroservice.facebook.FbGraphApiService;
import com.zik.faro.frontend.R;
import com.zik.faro.frontend.handlers.UserFriendListHandler;
import com.zik.faro.frontend.ui.activities.AddFriendsActivity;
import com.zik.faro.frontend.data.FacebookMinUser;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

/**
 * Created by gaurav on 7/15/17.
 */

public class AddFacebookFriendsFragment extends Fragment {
    private static final String TAG = "FacebookFriendsFragment";
    private Activity addFriendsActivity;
    private View fragmentView;

    // App Link for what should be opened when the recipient clicks on the install/play button on the app invite page.
    private static final String FACEBOOK_FARO_APP_LINK_URL = "https://fb.me/722765844577264";
    // A URL to an image to be used in the invite.
    private static final String FACEBOOK_FARO_APP_PREVIEW_IMAGE_URL = "";

    private TextView inviteFacebookFriends;
    private ListView facebookFriendsList;
    private FacebookFriendsListAdapter fbFriendsListAdapter;
    private Button addFacebookFriendsDoneButton;
    private Map<String, MinUser> selectedFriends = Maps.newHashMap();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new FaroExceptionHandler(getActivity()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        removeAllSelectedFriends();

        fragmentView = inflater.inflate(R.layout.fragment_facebookfriends_list, container, false);

        // setup text view button for inviting friends to app
        inviteFacebookFriends = (TextView) fragmentView.findViewById(R.id.inviteFacebookFriendsToAppButton);
        inviteFacebookFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Initiate workflow for inviting Facebook friends to Faro app");

                if (AppInviteDialog.canShow()) {
                    AppInviteContent content = new AppInviteContent.Builder()
                            .setApplinkUrl(FACEBOOK_FARO_APP_LINK_URL)
                            .setPreviewImageUrl(FACEBOOK_FARO_APP_PREVIEW_IMAGE_URL)
                            .build();
                    AppInviteDialog.show(getActivity(), content);
                } else {
                    Log.i(TAG, "Unable to show AppInvite Dialog");
                }
            }
        });

        // setup the friends list and its list adapter
        fbFriendsListAdapter = new FacebookFriendsListAdapter(getActivity(), R.layout.friend_row_style);
        facebookFriendsList = (ListView) fragmentView.findViewById(R.id.facebookFriendsList);
        facebookFriendsList.setAdapter(fbFriendsListAdapter);
        facebookFriendsList.setBackgroundColor(Color.BLUE);

        addFacebookFriendsDoneButton = (Button) fragmentView.findViewById(R.id.addFacebookFriendsDoneButton);
        addFacebookFriendsDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!selectedFriends.isEmpty()) {
                    addFacebookFriends();
                }

                // Complete this Activity and go back to the previous page
                getActivity().finish();
            }
        });
        addFacebookFriendsDoneButton.setVisibility(View.GONE);

        return fragmentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        addFriendsActivity = getActivity();

        // Load all friends into view
        loadFacebookFriendsIntoView();
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
            public void onResponse(final List<MinUser> minUsers, HttpError httpError) {
                if (httpError == null) {
                    Handler mainHandler = new Handler(addFriendsActivity.getMainLooper());
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "successfully established friend relation with facebook friends");
                            for (MinUser fbFriends : minUsers) {
                                UserFriendListHandler.getInstance().addFriendToListAndMap(fbFriends);
                            }
                        }
                    });
                } else {
                    Log.e(TAG, MessageFormat.format("Failed to establish friend relation with facebook friends. error = {0}", httpError));
                }
            }
        }, facebookFriendIds);
    }

    private void showAddFriendsDoneButton(boolean visible) {
        if (visible) {
            ((AddFriendsActivity) getActivity()).showAddByEmailButton(false);
            addFacebookFriendsDoneButton.setVisibility(View.VISIBLE);
        } else {
            addFacebookFriendsDoneButton.setVisibility(View.GONE);
            ((AddFriendsActivity) getActivity()).showAddByEmailButton(true);
        }
    }

    private void addSelectedFriend(MinUser minUser) {
        selectedFriends.put(((FacebookMinUser)minUser).getFacebookUserId(), minUser);
        showAddFriendsDoneButton(true);
    }

    private void removeSelectedFriend(MinUser minUser) {
        selectedFriends.remove(((FacebookMinUser)minUser).getFacebookUserId());

        if (selectedFriends.isEmpty()) {
            showAddFriendsDoneButton(false);
        }
    }

    private void removeAllSelectedFriends() {
        selectedFriends.clear();
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
                row = inflater.inflate(R.layout.add_friends_row_style, parent, false);
                holder = new FbFriendHolder(row, position);
                row.setTag(holder);
            } else {
                holder = (FbFriendHolder) row.getTag();
            }

            MinUser minUser = getItem(position);
            if (minUser != null) {
                if (minUser.getFirstName() != null) {
                    holder.setFriendName((minUser.getLastName() != null) ? MessageFormat.format("{0} {1}", minUser.getFirstName(), minUser.getLastName()) : minUser.getFirstName());
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

        if (accessToken != null) {
            final FbGraphApiService fbGraphApiService = new FbGraphApiService();
            fbGraphApiService.findFacebookFriends(new GraphRequest.Callback() {
                public void onCompleted(final GraphResponse response) {
                    // handle the result
                    Handler mainHandler = new Handler(addFriendsActivity.getMainLooper());
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            fbFriendsListAdapter.addAll(fbGraphApiService.getFriendsFromGraphResponse(response));
                        }
                    });
                }
            });
        }
    }

    private class FbFriendHolder {
        private ImageView friendPictureImageView;
        private TextView friendNameTextView;
        private CheckBox friendSelectionCheckBox;

        public FbFriendHolder(View rowInListView, int position) {
            friendPictureImageView = (ImageView)rowInListView.findViewById(R.id.contactFriendPicture);
            friendNameTextView = (TextView)rowInListView.findViewById(R.id.contactFriendName);
            friendSelectionCheckBox = (CheckBox)rowInListView.findViewById(R.id.contactFriendSelection);
            friendSelectionCheckBox.setTag(position);
            friendSelectionCheckBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "onItemClick");

                    CheckBox selectedCheckbox = (CheckBox) view;
                    FacebookMinUser facebookMinUser = (FacebookMinUser) fbFriendsListAdapter.getItem((Integer) selectedCheckbox.getTag());

                    if (selectedCheckbox.isChecked()) {
                        Log.i(TAG, MessageFormat.format("Selected facebook friend id = {0} firstName = {1}", facebookMinUser.getFacebookUserId(),
                                facebookMinUser.getFirstName()));

                        addSelectedFriend(facebookMinUser);
                    } else {
                        Log.i(TAG, MessageFormat.format("Unselected facebook friend id = {0} firstName = {1}", facebookMinUser.getFacebookUserId(),
                                facebookMinUser.getFirstName()));
                        removeSelectedFriend(facebookMinUser);
                    }
                }
            });
        }

        public void setFriendName(String friendName) {
            friendNameTextView.setText(friendName);
        }

        public ImageView getImageView() {
            return friendPictureImageView;
        }
    }
}
