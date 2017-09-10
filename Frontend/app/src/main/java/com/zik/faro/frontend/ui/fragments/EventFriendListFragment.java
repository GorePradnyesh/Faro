package com.zik.faro.frontend.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.zik.faro.data.Event;
import com.zik.faro.data.InviteeList;
import com.zik.faro.frontend.handlers.EventFriendListHandler;
import com.zik.faro.frontend.handlers.EventListHandler;
import com.zik.faro.frontend.FaroIntentConstants;
import com.zik.faro.frontend.R;
import com.zik.faro.frontend.ui.activities.UserProfilePage;
import com.zik.faro.frontend.ui.adapters.EventFriendAdapter;
import com.zik.faro.frontend.faroservice.notification.NotificationPayloadHandler;
import com.zik.faro.frontend.util.FaroIntentInfoBuilder;
import com.zik.faro.frontend.util.FaroObjectNotFoundException;

import java.text.MessageFormat;

import static android.widget.Toast.LENGTH_LONG;


/**
 * A simple {@link Fragment} subclass.
 */
public class EventFriendListFragment extends Fragment implements NotificationPayloadHandler {
    private String listStatus;
    private String eventId;
    private Context mContext;
    private View fragmentView = null;

    private LinearLayout linlaHeaderProgress = null;
    private RelativeLayout eventFriendListRelativeLayout = null;
    private Bundle extras = null;
    private String bundleType = null;
    private EventListHandler eventListHandler = EventListHandler.getInstance(getActivity());
    private Event cloneEvent;

    private EventFriendListHandler eventFriendListHandler = EventFriendListHandler.getInstance();

    private String TAG = "EvntFrndListFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_event_friend_list, container, false);

        linlaHeaderProgress = (LinearLayout) fragmentView.findViewById(R.id.linlaHeaderProgress);

        eventFriendListRelativeLayout = (RelativeLayout) fragmentView.findViewById(R.id.eventFriendListRelativeLayout);
        eventFriendListRelativeLayout.setVisibility(View.GONE);

        try {
            checkAndHandleNotification();
        } catch (FaroObjectNotFoundException e) {
            // Event has been deleted.
            Toast.makeText(getActivity(), "Event has been deleted", LENGTH_LONG).show();
            Log.e(TAG, MessageFormat.format("Event {0} has been deleted", eventId));
            getActivity().finish();
        }

        return fragmentView;
    }

    private void setupPageDetails () throws FaroObjectNotFoundException{
        linlaHeaderProgress.setVisibility(View.GONE);
        eventFriendListRelativeLayout.setVisibility(View.VISIBLE);

        cloneEvent = eventListHandler.getCloneObject(eventId);

        ListView guestList = (ListView) fragmentView.findViewById(R.id.guestList);
        guestList.setBackgroundColor(Color.BLACK);
        EventFriendAdapter eventFriendAdapter = eventFriendListHandler.getEventFriendAdapter(eventId, listStatus, mContext);
        guestList.setTag("EventFriendListFragment");
        guestList.setAdapter(eventFriendAdapter);

        guestList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Intent UserProfilePageIntent = new Intent(getActivity(), UserProfilePage.class);
                InviteeList.Invitees invitees = (InviteeList.Invitees) parent.getItemAtPosition(position);
                FaroIntentInfoBuilder.userProfileIntent(UserProfilePageIntent, invitees.getEmail(),
                        eventId);
                startActivity(UserProfilePageIntent);
            }
        });
    }

    @Override
    public void checkAndHandleNotification() throws FaroObjectNotFoundException{

        extras = getArguments();
        if (extras == null)return; //TODO: How to handle such conditions

        listStatus = extras.getString(FaroIntentConstants.LIST_STATUS);
        eventId = extras.getString(FaroIntentConstants.EVENT_ID);
        bundleType = extras.getString(FaroIntentConstants.BUNDLE_TYPE);

        if (FaroIntentConstants.IS_NOT_NOTIFICATION.equals(bundleType)){
            setupPageDetails();
        } else {
            //TODO: Handle this case if needed
            setupPageDetails();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Check if the version is same. It can be different if this page is loaded and a notification
        // is received for this later which updates the cache but clonedata on this page remains
        // stale.

        try {
            if (!eventListHandler.checkObjectVersionIfLatest(eventId, cloneEvent.getVersion())) {
                setupPageDetails();
            }
        } catch (FaroObjectNotFoundException e) {
            // Event has been deleted.
            Toast.makeText(getActivity(), "Event has been deleted", LENGTH_LONG).show();
            Log.e(TAG, MessageFormat.format("Event {0} has been deleted", eventId));
            getActivity().finish();
        }
    }
}
