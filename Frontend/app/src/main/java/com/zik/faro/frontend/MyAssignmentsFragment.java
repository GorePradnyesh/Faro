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
import android.widget.ListView;
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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyAssignmentsFragment extends Fragment{

    private AssignmentListHandler assignmentListHandler = AssignmentListHandler.getInstance();
    private EventListHandler eventListHandler = EventListHandler.getInstance();
    private FaroServiceHandler serviceHandler = FaroServiceHandler.getFaroServiceHandler();
    private ActivityListHandler activityListHandler = ActivityListHandler.getInstance();

    private ListView itemList;
    private Map<String, List<Item>> activityToItemListMap = new HashMap<>();

    private String eventID = null;
    private String passedActivityID = null;
    private String passedAssignmentID = null;

    private FaroUserContext faroUserContext = FaroUserContext.getInstance();
    private String myUserID = faroUserContext.getEmail();

    private static String TAG = "MyAssignmentsFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.my_assignments_fragment, container, false);
        super.onCreate(savedInstanceState);

        Thread.setDefaultUncaughtExceptionHandler(new FaroExceptionHandler(getActivity()));

        eventID = getArguments().getString("eventID");
        passedActivityID = getArguments().getString("activityID");
        passedAssignmentID = getArguments().getString("assignmentID");

        final Context mContext = this.getActivity();

        final TextView assignmentDescription = (TextView) view.findViewById(R.id.assignmentDescription);
        assignmentDescription.setText("My Items");

        Button updateAssignment = (Button) view.findViewById(R.id.updateAssignment);

        final Intent AssignmentLandingPageReloadIntent = new Intent(getActivity(), AssignmentLandingPage.class);

        itemList = (ListView) view.findViewById(R.id.itemList);
        itemList.setTag("MyAssignmentsFragment");
        final ItemsAdapter myItemsAdapter = new ItemsAdapter(getActivity(), R.layout.assignment_update_item_row_style);
        itemList.setAdapter(myItemsAdapter);


        //Walk all items in all assignments and add my cloned Items to the myItemsAdapter
        AssignmentAdapter assignmentAdapter = assignmentListHandler.getAssignmentAdapter(eventID, mContext);
        for (int i = 0; i < assignmentAdapter.list.size(); i++){
            AssignmentParentInfo assignmentParentInfo = assignmentAdapter.list.get(i);
            Assignment assignment = assignmentParentInfo.getAssignment();

            Assignment cloneAssignment = assignmentListHandler.getAssignmentCloneFromMap(assignment.getId());

            for (int j = 0; j < cloneAssignment.getItems().size(); j++){
                Item cloneItem = cloneAssignment.getItems().get(j);

                if (cloneItem.getAssigneeId().equals(myUserID)){
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
                activityToItemListMap.put(eventID, cloneAssignment.getItems());
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
                                        String activityID = entry.getKey();
                                        Assignment originalAssignment;
                                        if (activityID.equals(eventID)){
                                            Event originalEvent = eventListHandler.getOriginalEventFromMap(eventID);
                                            originalAssignment = originalEvent.getAssignment();
                                            originalAssignment.setItems(entry.getValue());
                                        }else {
                                            Activity originalActivity = activityListHandler.getOriginalActivityFromMap(activityID);
                                            originalAssignment = originalActivity.getAssignment();
                                            originalAssignment.setItems(entry.getValue());
                                        }
                                    }
                                    Log.i(TAG, "Successfully updated items list to server");

                                    //Reload AssignmentLandingFragment
                                    AssignmentLandingPageReloadIntent.putExtra("eventID", eventID);
                                    AssignmentLandingPageReloadIntent.putExtra("activityID", passedActivityID);
                                    AssignmentLandingPageReloadIntent.putExtra("assignmentID", passedAssignmentID);
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
                }, eventID, activityToItemListMap);
            }
        });

        return view;
    }
}
