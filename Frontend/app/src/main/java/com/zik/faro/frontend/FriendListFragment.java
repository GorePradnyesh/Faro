package com.zik.faro.frontend;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;

import com.zik.faro.data.MinUser;
import com.zik.faro.frontend.faroservice.auth.FaroUserContext;
import com.zik.faro.frontend.util.FaroIntentInfoBuilder;

public class FriendListFragment extends Fragment {
    private UserFriendListHandler userFriendListHandler = UserFriendListHandler.getInstance();
    private static String TAG = "FriendListFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_list, container, false);

        ListView friendListView  = (ListView)view.findViewById(R.id.friendList);
        friendListView.setBackgroundColor(Color.BLACK);
        friendListView.setAdapter(userFriendListHandler.userFriendAdapter);

        Thread.setDefaultUncaughtExceptionHandler(new FaroExceptionHandler(getActivity()));

        friendListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Intent userProfilePageIntent = new Intent(getActivity(), UserProfilePage.class);
                MinUser minUser = (MinUser) parent.getItemAtPosition(position);
                FaroIntentInfoBuilder.userProfileIntent(userProfilePageIntent, minUser.getEmail(), null);
                startActivity(userProfilePageIntent);
            }
        });

        // Setup invite friend button
        ImageButton inviteFriendButton = (ImageButton)view.findViewById(R.id.inviteFriend);
        inviteFriendButton.setImageResource(R.drawable.plus);

        inviteFriendButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent inviteFriendsIntent = new Intent(getActivity(), AddFriendsActivity.class);
                startActivity(inviteFriendsIntent);
            }
        });

        return view;
    }
}
