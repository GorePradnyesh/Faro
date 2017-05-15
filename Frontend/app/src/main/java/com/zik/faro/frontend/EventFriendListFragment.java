package com.zik.faro.frontend;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.zik.faro.data.InviteeList;
import com.zik.faro.data.MinUser;


/**
 * A simple {@link Fragment} subclass.
 */
public class EventFriendListFragment extends Fragment {
    private String guestListType;

    private static EventFriendListHandler eventFriendListHandler = EventFriendListHandler.getInstance();

    public EventFriendListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            guestListType = getArguments().getString("listType");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_event_friend_list, container, false);
        ListView guestList  = (ListView)view.findViewById(R.id.guestList);
        guestList.setBackgroundColor(Color.BLACK);
        EventFriendAdapter eventFriendAdapter = eventFriendListHandler.getEventFriendAdapter(guestListType);
        guestList.setAdapter(eventFriendAdapter);

        guestList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Intent UserProfilePageIntent = new Intent(getActivity(), UserProfilePage.class);
                InviteeList.Invitees invitees = (InviteeList.Invitees)parent.getItemAtPosition(position);
                UserProfilePageIntent.putExtra("userEmailID", invitees.getEmail());
                startActivity(UserProfilePageIntent);
            }
        });

        return view;
    }
}
