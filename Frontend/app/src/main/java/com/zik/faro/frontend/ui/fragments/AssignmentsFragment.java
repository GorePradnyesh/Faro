package com.zik.faro.frontend.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.zik.faro.data.Item;
import com.zik.faro.data.UpdateCollectionRequest;
import com.zik.faro.frontend.util.FaroExceptionHandler;
import com.zik.faro.frontend.FaroIntentConstants;
import com.zik.faro.frontend.R;
import com.zik.faro.frontend.ui.adapters.AssignmentAdapter;
import com.zik.faro.frontend.ui.adapters.ItemsAdapter;
import com.zik.faro.frontend.data.AssignmentParentInfo;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;
import com.zik.faro.frontend.faroservice.auth.FaroUserContext;
import com.zik.faro.frontend.faroservice.notification.NotificationPayloadHandler;
import com.zik.faro.frontend.util.FaroObjectNotFoundException;
import com.zik.faro.frontend.handlers.ActivityListHandler;
import com.zik.faro.frontend.handlers.AssignmentListHandler;
import com.zik.faro.frontend.handlers.EventListHandler;
import com.zik.faro.frontend.faroservice.notification.NotificationPayloadHandler;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.widget.Toast.LENGTH_LONG;

public class AssignmentsFragment extends Fragment implements NotificationPayloadHandler {
    private AssignmentListHandler assignmentListHandler = AssignmentListHandler.getInstance();
    private EventListHandler eventListHandler = EventListHandler.getInstance(getActivity());
    private FaroServiceHandler serviceHandler = FaroServiceHandler.getFaroServiceHandler();
    private ActivityListHandler activityListHandler = ActivityListHandler.getInstance();

    private ListView itemList;
    private Map<String, List<Item>> activityToItemListMap = new HashMap<>();
    private List<Item> eventAssignmentItems = new ArrayList<>();

    private String eventId = null;
    private String activityId = null;
    private String assignmentId = null;

    private FaroUserContext faroUserContext = FaroUserContext.getInstance();
    private String myUserId = faroUserContext.getEmail();

    private static String TAG = "AssignmentsFragment";
    private String bundleType = null;
    private Context mContext;
    private Activity cloneActivity = null;
    private Event cloneEvent = null;
    private View fragmentView;

    private LinearLayout linlaHeaderProgress = null;
    private RelativeLayout myAssignmentLandingFragmentRelativeLayout = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mContext = this.getActivity();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.my_assignments_fragment, container, false);
        super.onCreate(savedInstanceState);

        Thread.setDefaultUncaughtExceptionHandler(new FaroExceptionHandler(getActivity()));

        linlaHeaderProgress = (LinearLayout)fragmentView.findViewById(R.id.linlaHeaderProgress);
        myAssignmentLandingFragmentRelativeLayout = (RelativeLayout) fragmentView.findViewById(R.id.myAssignmentLandingFragmentRelativeLayout);
        myAssignmentLandingFragmentRelativeLayout.setVisibility(View.GONE);

        try {
            checkAndHandleNotification();
        } catch (FaroObjectNotFoundException e) {
            Toast.makeText(mContext, (activityId == null) ? "Event" : "Activity" + " has been deleted", LENGTH_LONG).show();
            Log.e(TAG, MessageFormat.format("{0} {1} has been deleted",
                    (activityId == null) ? "Event" : "Activity",
                    (activityId == null) ? eventId : activityId));
            getActivity().finish();
        }

        return fragmentView;
    }

    private void setupPageDetails () {
        linlaHeaderProgress.setVisibility(View.GONE);
        myAssignmentLandingFragmentRelativeLayout.setVisibility(View.VISIBLE);

        final TextView assignmentDescription = (TextView) fragmentView.findViewById(R.id.assignmentDescription);
        assignmentDescription.setText("My Items");

        Button updateAssignment = (Button) fragmentView.findViewById(R.id.updateAssignment);

        itemList = (ListView) fragmentView.findViewById(R.id.itemList);
        itemList.setTag("AssignmentsFragment");
        final ItemsAdapter myItemsAdapter = new ItemsAdapter(getActivity(), R.layout.assignment_update_item_row_style);
        itemList.setAdapter(myItemsAdapter);


        //Walk all items in all assignments and add my cloned Items to the myItemsAdapter
        AssignmentAdapter assignmentAdapter = assignmentListHandler.getAssignmentAdapter(eventId, mContext);
        for (int i = 0; i < assignmentAdapter.list.size(); i++){
            AssignmentParentInfo assignmentParentInfo = assignmentAdapter.list.get(i);
            Assignment assignment = assignmentParentInfo.getAssignment();

            Assignment cloneAssignment = assignmentListHandler.getAssignmentCloneFromMap(assignment.getId());

            for (int j = 0; j < cloneAssignment.getItems().size(); j++){
                Item cloneItem = cloneAssignment.getItems().get(j);

                if (cloneItem.getAssigneeId().equals(myUserId)){
                    if (cloneItem.getStatus().equals(ActionStatus.COMPLETE)) {
                        myItemsAdapter.insertAtEnd(cloneItem);
                    } else {
                        myItemsAdapter.insertAtBeginning(cloneItem);
                    }
                }
            }
            if (assignmentParentInfo.getActivityID() != null) {
                activityToItemListMap.put(assignmentParentInfo.getActivityID(), cloneAssignment.getItems());
            } else {
                eventAssignmentItems.addAll(cloneAssignment.getItems());
            }
        }

        updateAssignment.setOnClickListener(new View.OnClickListener() {
            // TODO : Update of assignments status here is broken. This will change based on the UI design
            @Override
            public void onClick(View view) {
                /*if (!activityToItemListMap.isEmpty()) {
                    UpdateCollectionRequest<Activity, Item> activityUpdateCollectionRequest = new UpdateCollectionRequest<>();
                    activityUpdateCollectionRequest.setToBeAdded(activityToItemListMap.get(0));
                    try {
                        activityUpdateCollectionRequest.setUpdate(activityListHandler.getOriginalObject(activityId));
                    } catch (FaroObjectNotFoundException e) {
                        Log.e(TAG, "Failed to update activity assignment", e);
                        return;
                    }

                    serviceHandler.getActivityHandler().updateActivityAssignment(new BaseFaroRequestCallback<Activity>() {
                        @Override
                        public void onFailure(Request request, IOException ex) {
                            Log.e(TAG, "failed to send new item list", ex);
                        }

                        @Override
                        public void onResponse(final Activity activity, HttpError error) {
                            if (error == null ) {
                                Handler mainHandler = new Handler(mContext.getMainLooper());
                                mainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.i(TAG, "Successfully updated items list to server");

                                        try {
                                            Activity originalActivity = activityListHandler.getOriginalObject(activity.getId());
                                            Assignment originalAssignment = originalActivity.getAssignment();
                                            originalAssignment.setItems(activity.getAssignment().getItems());

                                            setupPageDetails();
                                        } catch (FaroObjectNotFoundException e) {
                                            // Activity has been deleted.
                                            Toast.makeText(getActivity(),"Activity has been deleted", LENGTH_LONG).show();
                                            Log.e(TAG, MessageFormat.format("Activity {0} has been deleted", activity.getId()));
                                            getActivity().finish();
                                        }
                                    }
                                });
                            } else {
                                Log.e(TAG, MessageFormat.format("code = {0} , message =  {1}", error.getCode(), error.getMessage()));
                            }
                        }
                    }, eventId, activityId, activityUpdateCollectionRequest);
                }

                if (!eventAssignmentItems.isEmpty()) {
                    UpdateCollectionRequest<Event, Item> eventUpdateCollectionRequest = new UpdateCollectionRequest<>();
                    eventUpdateCollectionRequest.setToBeAdded(eventAssignmentItems);
                    try {
                        eventUpdateCollectionRequest.setUpdate(eventListHandler.getOriginalObject(eventId));
                    } catch (FaroObjectNotFoundException e) {
                        Log.e(TAG, "Failed to update event assignment", e);
                        return;
                    }

                    serviceHandler.getAssignmentHandler().updateAssignment(new BaseFaroRequestCallback<Event>() {
                        @Override
                        public void onFailure(Request request, IOException ex) {
                            Log.e(TAG, "failed to send new item list", ex);
                        }

                        @Override
                        public void onResponse(final Event event, HttpError error) {
                            if (error == null ) {
                                Handler mainHandler = new Handler(mContext.getMainLooper());
                                mainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.i(TAG, "Successfully updated items list to server");

                                        try {
                                            Event originalEvent = eventListHandler.getOriginalObject(event.getId());
                                            Assignment originalAssignment = originalEvent.getAssignment();
                                            originalAssignment.setItems(event.getAssignment().getItems());

                                            setupPageDetails();
                                        } catch (FaroObjectNotFoundException e) {
                                            // Event has been deleted.
                                            Toast.makeText(getActivity(), "Event has been deleted", LENGTH_LONG).show();
                                            Log.e(TAG, MessageFormat.format("Event {0} has been deleted", event.getId()));
                                            getActivity().finish();
                                        }
                                    }
                                });
                            } else {
                                Log.e(TAG, MessageFormat.format("code = {0} , message =  {1}", error.getCode(), error.getMessage()));
                            }
                        }
                    }, eventId, eventUpdateCollectionRequest);
                }*/

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

        if (activityId == null){
            cloneEvent = eventListHandler.getCloneObject(eventId);
        } else {
            cloneActivity = activityListHandler.getCloneObject(activityId);
        }

        setupPageDetails();
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
                //Activity has been deleted.
                Toast.makeText(mContext, (activityId == null) ? "Event" : "Activity" + " has been deleted", LENGTH_LONG).show();
                Log.e(TAG, MessageFormat.format("{0} {1} has been deleted",
                        activityId == null ? "Event" : "Activity",
                        activityId == null ? eventId : activityId));
                getActivity().finish();
            }
        }
    }
}
