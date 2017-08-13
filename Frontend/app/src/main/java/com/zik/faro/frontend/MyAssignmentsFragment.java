package com.zik.faro.frontend;

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

import com.squareup.okhttp.Request;
import com.zik.faro.data.ActionStatus;
import com.zik.faro.data.Activity;
import com.zik.faro.data.Assignment;
import com.zik.faro.data.Event;
import com.zik.faro.data.Item;
import com.zik.faro.frontend.data.AssignmentParentInfo;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;
import com.zik.faro.frontend.faroservice.auth.FaroUserContext;
import com.zik.faro.frontend.notification.NotificationPayloadHandler;
import com.zik.faro.frontend.util.FaroObjectNotFoundException;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyAssignmentsFragment extends Fragment implements NotificationPayloadHandler {

    private AssignmentListHandler assignmentListHandler = AssignmentListHandler.getInstance();
    private EventListHandler eventListHandler = EventListHandler.getInstance();
    private FaroServiceHandler serviceHandler = FaroServiceHandler.getFaroServiceHandler();
    private ActivityListHandler activityListHandler = ActivityListHandler.getInstance();

    private ListView itemList;
    private Map<String, List<Item>> activityToItemListMap = new HashMap<>();

    private String eventId = null;
    private String activityId = null;
    private String assignmentId = null;

    private FaroUserContext faroUserContext = FaroUserContext.getInstance();
    private String myUserId = faroUserContext.getEmail();

    private static String TAG = "MyAssignmentsFragment";
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

        checkAndHandleNotification();

        return fragmentView;
    }

    private void setupPageDetails () {
        linlaHeaderProgress.setVisibility(View.GONE);
        myAssignmentLandingFragmentRelativeLayout.setVisibility(View.VISIBLE);

        final TextView assignmentDescription = (TextView) fragmentView.findViewById(R.id.assignmentDescription);
        assignmentDescription.setText("My Items");

        Button updateAssignment = (Button) fragmentView.findViewById(R.id.updateAssignment);

        itemList = (ListView) fragmentView.findViewById(R.id.itemList);
        itemList.setTag("MyAssignmentsFragment");
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
            }else{
                activityToItemListMap.put(eventId, cloneAssignment.getItems());
            }
        }

        updateAssignment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                                    for(Map.Entry<String, List<Item>> entry: stringListMap.entrySet()){
                                        String objectId = entry.getKey();
                                        Assignment originalAssignment;
                                        try {
                                            if (objectId.equals(eventId)){
                                                Event originalEvent = (Event) eventListHandler.getOriginalObject(eventId);
                                                originalAssignment = originalEvent.getAssignment();
                                                originalAssignment.setItems(entry.getValue());
                                            } else {
                                                Activity originalActivity = (Activity) activityListHandler.getOriginalObject(objectId);
                                                originalAssignment = originalActivity.getAssignment();
                                                originalAssignment.setItems(entry.getValue());
                                            }
                                        } catch (FaroObjectNotFoundException e) {
                                            //Activity has been deleted.
                                            Log.i(TAG, MessageFormat.format("{0} {1} has been deleted",
                                                    objectId.equals(eventId) ? "Event" : "Activity",
                                                    objectId.equals(eventId) ? eventId : objectId));
                                            getActivity().finish();
                                        }
                                    }
                                    Log.i(TAG, "Successfully updated items list to server");

                                    setupPageDetails();
                                }
                            };
                            Handler mainHandler = new Handler(mContext.getMainLooper());
                            mainHandler.post(myRunnable);
                        }else {
                            Log.i(TAG, "code = " + error.getCode() + ", message = " + error.getMessage());
                        }
                    }
                }, eventId, activityToItemListMap);
            }
        });
    }


    @Override
    public void checkAndHandleNotification() {
        Bundle extras = getArguments();
        if (extras == null) return; //TODO: How to handle such conditions

        eventId = extras.getString(FaroIntentConstants.EVENT_ID);
        activityId = extras.getString(FaroIntentConstants.ACTIVITY_ID);
        assignmentId = extras.getString(FaroIntentConstants.ASSIGNMENT_ID);
        bundleType = extras.getString(FaroIntentConstants.BUNDLE_TYPE);


        try {
            if (activityId == null){
                cloneEvent = eventListHandler.getCloneObject(eventId);
            } else {
                cloneActivity = activityListHandler.getCloneObject(activityId);
            }
        } catch (FaroObjectNotFoundException e) {
            Log.i(TAG, MessageFormat.format("{0} {1} has been deleted",
                    (activityId == null) ? "Event" : "Activity",
                    (activityId == null) ? eventId : activityId));
            getActivity().finish();
        }


        setupPageDetails();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (bundleType.equals(FaroIntentConstants.IS_NOT_NOTIFICATION)) {
            // Check if the version is same. It can be different if this page is loaded and a notification
            // is received for this later which updates the global memory but clonedata on this page remains
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
                Log.i(TAG, MessageFormat.format("{0} {1} has been deleted",
                        activityId == null ? "Event" : "Activity",
                        activityId == null ? eventId : activityId));
                getActivity().finish();
            }
        }
    }
}
