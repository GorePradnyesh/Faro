package com.zik.faro.frontend.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.Request;
import com.zik.faro.data.ActionStatus;
import com.zik.faro.data.Activity;
import com.zik.faro.data.Assignment;
import com.zik.faro.data.Event;
import com.zik.faro.data.EventInviteStatusWrapper;
import com.zik.faro.data.InviteeList;
import com.zik.faro.data.Item;
import com.zik.faro.frontend.handlers.AssignmentListHandler;
import com.zik.faro.frontend.handlers.EventFriendListHandler;
import com.zik.faro.frontend.handlers.EventListHandler;
import com.zik.faro.frontend.ui.activities.EditAssignmentActivity;
import com.zik.faro.frontend.util.FaroExceptionHandler;
import com.zik.faro.frontend.FaroIntentConstants;
import com.zik.faro.frontend.ui.adapters.ItemsAdapter;
import com.zik.faro.frontend.R;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;
import com.zik.faro.frontend.handlers.ActivityListHandler;
import com.zik.faro.frontend.faroservice.notification.NotificationPayloadHandler;
import com.zik.faro.frontend.util.FaroIntentInfoBuilder;
import com.zik.faro.frontend.util.FaroObjectNotFoundException;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.widget.Toast.LENGTH_LONG;

public class AssignmentLandingFragment extends Fragment implements NotificationPayloadHandler {

    private Event originalEvent;
    private com.zik.faro.data.Activity originalActivity = null;
    private String activityId = null;
    private String eventId = null;
    private String assignmentId = null;
    private Assignment cloneAssignment = null;

    private EventListHandler eventListHandler = EventListHandler.getInstance(getActivity());
    private ActivityListHandler activityListHandler = ActivityListHandler.getInstance();
    private AssignmentListHandler assignmentListHandler = AssignmentListHandler.getInstance();
    private FaroServiceHandler serviceHandler = FaroServiceHandler.getFaroServiceHandler();
    private EventFriendListHandler eventFriendListHandler = EventFriendListHandler.getInstance();


    private static String TAG = "AssgnmntLandingFrgmnt";

    private ListView itemList;
    private Map<String, List<Item>> itemListMap = new HashMap<String, List<Item>>();

    private Context mContext;
    private View fragmentView;
    private RelativeLayout assignmentLandingFragmentRelativeLayout = null;
    private LinearLayout linlaHeaderProgress = null;
    private String bundleType = null;
    private boolean receivedAllActivities = false;
    private boolean receivedEvent = false;
    private Activity cloneActivity = null;
    private Event cloneEvent = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mContext = this.getActivity();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.assignment_landing_fragment, container, false);
        super.onCreate(savedInstanceState);

        Thread.setDefaultUncaughtExceptionHandler(new FaroExceptionHandler(getActivity()));

        linlaHeaderProgress = (LinearLayout)fragmentView.findViewById(R.id.linlaHeaderProgress);
        assignmentLandingFragmentRelativeLayout = (RelativeLayout) fragmentView.findViewById(R.id.assignmentLandingFragmentRelativeLayout);
        assignmentLandingFragmentRelativeLayout.setVisibility(View.GONE);

        try {
            checkAndHandleNotification();
        } catch (FaroObjectNotFoundException e) {
            Log.e(TAG, MessageFormat.format("{0} {1} has been deleted",
                    activityId == null ? "Event" : "Activity",
                    activityId == null ? eventId : activityId));
            Toast.makeText(getActivity(), activityId == null ? "Event" : "Activity" + " has been deleted", LENGTH_LONG).show();
            getActivity().finish();
        }

        return fragmentView;
    }

    private void setupPageDetails() throws FaroObjectNotFoundException{

        if (!receivedEvent || !receivedAllActivities)
            return;

        linlaHeaderProgress.setVisibility(View.GONE);
        assignmentLandingFragmentRelativeLayout.setVisibility(View.VISIBLE);

        cloneAssignment = assignmentListHandler.getAssignmentCloneFromMap(assignmentId);
        originalEvent = (Event) eventListHandler.getOriginalObject(eventId);

        final TextView assignmentDescription = (TextView) fragmentView.findViewById(R.id.assignmentDescription);
        if (activityId != null) {
            originalActivity = (Activity) activityListHandler.getOriginalObject(activityId);
            assignmentDescription.setText("Assignment for " + originalActivity.getName());
        } else {
            assignmentDescription.setText("Assignment for " + originalEvent.getEventName());
        }

        ImageButton editButton = (ImageButton) fragmentView.findViewById(R.id.editButton);
        editButton.setImageResource(R.drawable.edit);

        Button updateAssignment = (Button) fragmentView.findViewById(R.id.updateAssignment);

        final Intent EditAssignmentPage = new Intent(getActivity(), EditAssignmentActivity.class);

        itemList = (ListView) fragmentView.findViewById(R.id.itemList);
        itemList.setTag("AssignmentLandingPageFragment");
        final ItemsAdapter itemsAdapter = new ItemsAdapter(getActivity(), R.layout.assignment_update_item_row_style);
        itemList.setAdapter(itemsAdapter);

        final List<Item> cloneItemsList = cloneAssignment.getItems();
        for (Integer i = cloneItemsList.size() - 1; i >= 0; i--) {
            Item item = cloneItemsList.get(i);
            if (item.getStatus().equals(ActionStatus.COMPLETE)) {
                itemsAdapter.insertAtEnd(item);
            } else {
                itemsAdapter.insertAtBeginning(item);
            }
        }

        updateAssignment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activityId != null) {
                    itemListMap.put(activityId, cloneItemsList);
                }else {
                    itemListMap.put(eventId, cloneItemsList);
                }
                updateAssignmentToServer();
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FaroIntentInfoBuilder.assignmentIntent(EditAssignmentPage,
                        eventId, activityId, assignmentId);
                startActivity(EditAssignmentPage);
                getActivity().finish();
            }
        });
    }


    @Override
    public void checkAndHandleNotification() throws FaroObjectNotFoundException{
        Bundle extras = getArguments();
        if (extras == null) return; //TODO: How to handle such conditions

        eventId = extras.getString(FaroIntentConstants.EVENT_ID);
        activityId = extras.getString(FaroIntentConstants.ACTIVITY_ID);
        assignmentId = extras.getString(FaroIntentConstants.ASSIGNMENT_ID);
        bundleType = extras.getString(FaroIntentConstants.BUNDLE_TYPE);

        if (activityId == null) {
            cloneEvent = eventListHandler.getCloneObject(eventId);
        } else {
            cloneActivity = activityListHandler.getCloneObject(activityId);
        }

        if (bundleType.equals(FaroIntentConstants.IS_NOT_NOTIFICATION)){
            receivedAllActivities = true;
            receivedEvent = true;
            setupPageDetails();
        } else {
            // Else the bundleType is "notification"
            // Do an API call to get all assignments for all activities for this event and also the
            // event assignment. So get the event as well
            getEventActivitiesFromServer();

            getEventFromServer();

            getEventInviteesFromServer();
        }
    }

    public void getEventActivitiesFromServer() {
        serviceHandler.getActivityHandler().getActivities(new BaseFaroRequestCallback<List<Activity>>() {
            @Override
            public void onFailure(Request request, IOException ex) {
                Log.e(TAG, "failed to get activity list");
            }

            @Override
            public void onResponse(final List<com.zik.faro.data.Activity> activities, HttpError error) {
                if (error == null) {
                    Handler mainHandler = new Handler(mContext.getMainLooper());
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "Successfully received activities from the server!!");
                            activityListHandler.addDownloadedActivitiesToListAndMap(eventId, activities, mContext);
                            receivedAllActivities = true;
                            try {
                                setupPageDetails();
                            } catch (FaroObjectNotFoundException e) {
                                // Event/Activity has been deleted.
                                Log.e(TAG, MessageFormat.format("{0} {1} has been deleted",
                                        activityId == null ? "Event" : "Activity",
                                        activityId == null ? eventId : activityId));
                                Toast.makeText(getActivity(), activityId == null ? "Event" : "Activity" + " has been deleted", LENGTH_LONG).show();
                                getActivity().finish();
                            }
                        }
                    });
                } else {
                    Log.e(TAG, MessageFormat.format("code = {0) , message =  {1}", error.getCode(), error.getMessage()));
                }
            }
        }, eventId);
    }

    public void getEventInviteesFromServer(){
        serviceHandler.getEventHandler().getEventInvitees(new BaseFaroRequestCallback<InviteeList>() {
            @Override
            public void onFailure(Request request, IOException ex) {
                Log.e(TAG, "failed to get cloneEvent Invitees");
            }

            @Override
            public void onResponse(final InviteeList inviteeList, HttpError error) {
                if (error == null) {
                    Handler mainHandler = new Handler(mContext.getMainLooper());
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "Successfully received Invitee List for the cloneEvent");
                            eventFriendListHandler.addDownloadedFriendsToListAndMap(eventId, inviteeList, mContext);
                        }
                    });
                } else {
                    Log.e(TAG, MessageFormat.format("code = {0) , message =  {1}", error.getCode(), error.getMessage()));
                }
            }
        }, eventId);
    }

    public void updateAssignmentToServer () {
        serviceHandler.getAssignmentHandler().updateAssignment(new BaseFaroRequestCallback<Map<String, List<Item>>>() {
            @Override
            public void onFailure(Request request, IOException ex) {
                Log.e(TAG, "failed to send new item list");
            }

            @Override
            public void onResponse(final Map<String, List<Item>> stringListMap, HttpError error) {
                if (error == null ) {
                    Handler mainHandler = new Handler(mContext.getMainLooper());
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "Successfully updated items list to server");
                            final Assignment originalAssignment = assignmentListHandler.getOriginalAssignmentFromMap(assignmentId);

                            List<Item> receivedItemList;
                            if (activityId != null) {
                                receivedItemList = stringListMap.get(activityId);
                            }else {
                                receivedItemList = stringListMap.get(eventId);
                            }

                            originalAssignment.setItems(receivedItemList);

                            try {
                                setupPageDetails();
                            } catch (FaroObjectNotFoundException e) {
                                // Event/Activity has been deleted.
                                Log.e(TAG, MessageFormat.format("{0} {1} has been deleted",
                                        activityId == null ? "Event" : "Activity",
                                        activityId == null ? eventId : activityId));
                                Toast.makeText(getActivity(), activityId == null ? "Event" : "Activity" + " has been deleted", LENGTH_LONG).show();
                                getActivity().finish();
                            }
                        }
                    });
                }else {
                    Log.e(TAG, MessageFormat.format("code = {0) , message =  {1}", error.getCode(), error.getMessage()));
                }
            }
        }, eventId, itemListMap);
    }


    public void getEventFromServer () {
        serviceHandler.getEventHandler().getEvent(new BaseFaroRequestCallback<EventInviteStatusWrapper>() {
            @Override
            public void onFailure(Request request, IOException ex) {
                Log.e(TAG, "failed to get Event from server");
            }

            @Override
            public void onResponse(final EventInviteStatusWrapper eventInviteStatusWrapper, HttpError error) {
                if (error == null ) {
                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "Successfully received Event from server");
                            receivedEvent = true;
                            Event event = eventInviteStatusWrapper.getEvent();
                            eventListHandler.addEventToListAndMap(event,
                                    eventInviteStatusWrapper.getInviteStatus());
                            assignmentListHandler.addAssignmentToListAndMap(eventId, event.getAssignment(), null, mContext);
                            try {
                                setupPageDetails();
                            } catch (FaroObjectNotFoundException e) {
                                // Event/Activity has been deleted.
                                Log.e(TAG, MessageFormat.format("{0} {1} has been deleted",
                                        activityId == null ? "Event" : "Activity",
                                        activityId == null ? eventId : activityId));
                                Toast.makeText(getActivity(), activityId == null ? "Event" : "Activity" + " has been deleted", LENGTH_LONG).show();
                                getActivity().finish();
                            }
                        }
                    };
                    Handler mainHandler = new Handler(mContext.getMainLooper());
                    mainHandler.post(myRunnable);
                }else {
                    Log.e(TAG, MessageFormat.format("code = {0) , message =  {1}", error.getCode(), error.getMessage()));
                }
            }
        }, eventId);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (bundleType.equals(FaroIntentConstants.IS_NOT_NOTIFICATION)) {
            // Check if the version is same. It can be different if this page is loaded and a notification
            // is received for this later which updates the cache but clonedata on this page remains
            // stale.
            // This check is not necessary when opening this page directly through a notification.
            try {
                if (activityId == null) {
                    if (!eventListHandler.checkObjectVersionIfLatest(eventId, cloneEvent.getVersion())) {
                        setupPageDetails();
                    }
                } else {
                    if (!activityListHandler.checkObjectVersionIfLatest(activityId, cloneActivity.getVersion())) {
                        setupPageDetails();
                    }
                }
            } catch (FaroObjectNotFoundException e) {
                // Event/Activity has been deleted.
                Log.e(TAG, MessageFormat.format("{0} {1} has been deleted",
                        activityId == null ? "Event" : "Activity",
                        activityId == null ? eventId : activityId));
                Toast.makeText(getActivity(), activityId == null ? "Event" : "Activity" + " has been deleted", LENGTH_LONG).show();
                getActivity().finish();
            }
        }
    }
}
