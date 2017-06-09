package com.zik.faro.frontend;

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

import com.squareup.okhttp.Request;
import com.zik.faro.data.ActionStatus;
import com.zik.faro.data.Activity;
import com.zik.faro.data.Assignment;
import com.zik.faro.data.Event;
import com.zik.faro.data.EventInviteStatusWrapper;
import com.zik.faro.data.Item;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;
import com.zik.faro.frontend.notification.NotificationPayloadHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssignmentLandingFragment extends Fragment implements NotificationPayloadHandler {

    private static Event originalEvent;
    private static com.zik.faro.data.Activity originalActivity = null;
    String activityID = null;
    String eventID = null;
    String assignmentID = null;
    private static Assignment cloneAssignment = null;

    private static EventListHandler eventListHandler = EventListHandler.getInstance();
    private static ActivityListHandler activityListHandler = ActivityListHandler.getInstance();
    private static AssignmentListHandler assignmentListHandler = AssignmentListHandler.getInstance();
    private static FaroServiceHandler serviceHandler = FaroServiceHandler.getFaroServiceHandler();

    private static String TAG = "AssgnmntLandingFrgmnt";

    private ListView itemList;
    private Map<String, List<Item>> itemListMap = new HashMap<String, List<Item>>();

    private Intent AssignmentLandingPageReloadIntent = null;

    private final Context mContext = this.getActivity();
    private View fragmentView;
    private RelativeLayout assignmentLandingFragmentRelativeLayout = null;
    private LinearLayout linlaHeaderProgress = null;
    private String isNotification = null;
    private boolean receivedAllActivities = false;
    private boolean receivedEvent = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
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

        checkAndHandleNotification();

        return fragmentView;
    }

    public void setupPageDetails(){

        if (!receivedEvent || !receivedAllActivities)
            return;

        linlaHeaderProgress.setVisibility(View.GONE);
        assignmentLandingFragmentRelativeLayout.setVisibility(View.VISIBLE);

        cloneAssignment = assignmentListHandler.getAssignmentCloneFromMap(assignmentID);
        originalEvent = eventListHandler.getOriginalEventFromMap(eventID);

        final TextView assignmentDescription = (TextView) fragmentView.findViewById(R.id.assignmentDescription);
        if (activityID != null) {
            originalActivity = activityListHandler.getOriginalActivityFromMap(activityID);
            assignmentDescription.setText("Assignment for " + originalActivity.getName());
        } else {
            assignmentDescription.setText("Assignment for " + originalEvent.getEventName());
        }

        ImageButton editButton = (ImageButton) fragmentView.findViewById(R.id.editButton);
        editButton.setImageResource(R.drawable.edit);

        Button updateAssignment = (Button) fragmentView.findViewById(R.id.updateAssignment);

        final Intent EditAssignmentPage = new Intent(getActivity(), EditAssignment.class);
        AssignmentLandingPageReloadIntent = new Intent(getActivity(), AssignmentLandingPage.class);

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
                if (activityID != null) {
                    itemListMap.put(activityID, cloneItemsList);
                }else {
                    itemListMap.put(eventID, cloneItemsList);
                }
                updateAssignmentToServer();
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditAssignmentPage.putExtra("eventID", eventID);
                EditAssignmentPage.putExtra("activityID", activityID);
                EditAssignmentPage.putExtra("assignmentID", assignmentID);
                EditAssignmentPage.putExtra("bundleType", isNotification);
                startActivity(EditAssignmentPage);
                getActivity().finish();
            }
        });
    }


    @Override
    public void checkAndHandleNotification() {
        Bundle extras = getArguments();
        if (extras == null) return; //TODO: How to handle such conditions

        eventID = extras.getString("eventID");
        activityID = extras.getString("activityID");
        assignmentID = extras.getString("assignmentID");
        isNotification = extras.getString("bundleType");

        if (isNotification == null){
            receivedAllActivities = true;
            receivedEvent = true;
            setupPageDetails();
        } else {
            // Else the bundleType is "notification"
            // Do an API call to get all assignments for all activities for this event and also the
            // event assignment. So get the event as well
            getEventActivitiesFromServer();

            getEventFromServer();
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
                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "Successfully received activities from the server!!");
                            activityListHandler.addDownloadedActivitiesToListAndMap(eventID, activities, mContext);
                            receivedAllActivities = true;
                            setupPageDetails();
                        }
                    };
                    Handler mainHandler = new Handler(mContext.getMainLooper());
                    mainHandler.post(myRunnable);
                } else {
                    Log.i(TAG, "code = " + error.getCode() + ", message = " + error.getMessage());
                }
            }
        }, eventID);
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
                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "Successfully updated items list to server");
                            final Assignment originalAssignment = assignmentListHandler.getOriginalAssignmentFromMap(assignmentID);

                            List<Item> receivedItemList;
                            if (activityID != null) {
                                receivedItemList = stringListMap.get(activityID);
                            }else {
                                receivedItemList = stringListMap.get(eventID);
                            }

                            originalAssignment.setItems(receivedItemList);

                            //Reload AssignmentLandingFragment
                            AssignmentLandingPageReloadIntent.putExtra("eventID", eventID);
                            AssignmentLandingPageReloadIntent.putExtra("activityID", activityID);
                            AssignmentLandingPageReloadIntent.putExtra("assignmentID", assignmentID);
                            getActivity().finish();
                            startActivity(AssignmentLandingPageReloadIntent);
                        }
                    };
                    Handler mainHandler = new Handler(mContext.getMainLooper());
                    mainHandler.post(myRunnable);
                }else {
                    Log.i(TAG, "code = " + error.getCode() + ", message = " + error.getMessage());
                }
            }
        }, eventID, itemListMap);
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
                            assignmentListHandler.addAssignmentToListAndMap(eventID, event.getAssignment(), null, mContext);
                            setupPageDetails();
                        }
                    };
                    Handler mainHandler = new Handler(mContext.getMainLooper());
                    mainHandler.post(myRunnable);
                }else {
                    Log.i(TAG, "code = " + error.getCode() + ", message = " + error.getMessage());
                }
            }
        }, eventID);
    }
}
