package com.zik.faro.frontend;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;


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
            guestListType = getArguments().getString("list_type");
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
        return view;
    }
}
