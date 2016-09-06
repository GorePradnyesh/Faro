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

import com.google.gson.Gson;
import com.squareup.okhttp.Request;
import com.zik.faro.data.ActionStatus;
import com.zik.faro.data.Activity;
import com.zik.faro.data.Assignment;
import com.zik.faro.data.Event;
import com.zik.faro.data.Item;
import com.zik.faro.frontend.data.AssignmentParentInfo;
import com.zik.faro.frontend.data.ItemParentInfo;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;
import com.zik.faro.frontend.faroservice.auth.FaroUserContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MyAssignmentsFragment extends Fragment{

    private static AssignmentListHandler assignmentListHandler = AssignmentListHandler.getInstance();
    private static EventListHandler eventListHandler = EventListHandler.getInstance();
    private static FaroServiceHandler serviceHandler = eventListHandler.serviceHandler;
    private static ActivityListHandler activityListHandler = ActivityListHandler.getInstance();

    private ListView itemList;
    private Map<String, List<Item>> assignmentToItemListMap = new HashMap<>();

    String eventID = null;
    String passedActivityID = null;
    String passedAssignmentID = null;

    FaroUserContext faroUserContext = FaroUserContext.getInstance();
    String myUserID = faroUserContext.getEmail();

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
        for (int i = 0; i < assignmentListHandler.assignmentAdapter.list.size(); i++){
            AssignmentParentInfo assignmentParentInfo = assignmentListHandler.assignmentAdapter.list.get(i);
            Assignment assignment = assignmentParentInfo.getAssignment();

            Assignment cloneAssignment = assignmentListHandler.getAssignmentCloneFromMap(assignment.getId());

            for (int j = 0; j < cloneAssignment.getItems().size(); j++){
                Item cloneItem = cloneAssignment.getItems().get(j);

                if (cloneItem.getAssigneeId().equals(myUserID)){
                    ItemParentInfo itemParentInfo = new ItemParentInfo(cloneItem, eventID,
                            assignmentParentInfo.getActivityID(), assignment.getId());
                    if (cloneItem.getStatus().equals(ActionStatus.COMPLETE)) {
                        myItemsAdapter.insertAtEnd(itemParentInfo);
                    } else {
                        myItemsAdapter.insertAtBeginning(itemParentInfo);
                    }
                }
            }
            assignmentToItemListMap.put(assignment.getId(), cloneAssignment.getItems());
        }

        updateAssignment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                for(Map.Entry<String, List<Item>> entry: assignmentToItemListMap.entrySet()){
                    String assignmentID = entry.getKey();
                    final Assignment assignment = assignmentListHandler.getOriginalAssignmentFromMap(assignmentID);

                    final List<Item> updatedItemList = entry.getValue();
                    final String activityID = assignmentListHandler.getActivityIDForAssignmentID(assignmentID);

                    serviceHandler.getAssignmentHandler().updateAssignment(new BaseFaroRequestCallback<String>() {
                        @Override
                        public void onFailure(Request request, IOException ex) {
                            Log.e(TAG, "failed to send new item list");
                        }

                        @Override
                        public void onResponse(String s, HttpError error) {
                            if (error == null ) {
                                Runnable myRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.i(TAG, "Successfully updated items list to server");
                                        assignment.setItems(updatedItemList);
                                        assignment.getItems();
                                    }
                                };
                                Handler mainHandler = new Handler(mContext.getMainLooper());
                                mainHandler.post(myRunnable);
                            }else {
                                Log.i(TAG, "code = " + error.getCode() + ", message = " + error.getMessage());
                            }
                        }
                    }, eventID, assignmentID, activityID, updatedItemList);
                }


                //Reload AssignmentLandingFragment
                /*TODO Reload only after all the updates to server have been done. If we reload before that then
                * there are chances that since the server has not responded the local object will not be updated
                * hence will show stale data. But this will automatically be corrected when the server responds.
                * But during that small amount of time the user will see stale data.
                * Could not implement this since require a shared variable between threads to identify the number
                * of updates done/remaining before we call reload the page.*/
                AssignmentLandingPageReloadIntent.putExtra("eventID", eventID);
                AssignmentLandingPageReloadIntent.putExtra("activityID", passedActivityID);
                AssignmentLandingPageReloadIntent.putExtra("assignmentID", passedAssignmentID);
                getActivity().finish();
                startActivity(AssignmentLandingPageReloadIntent);
            }
        });

        return view;
    }
}
