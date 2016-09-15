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
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.okhttp.Request;
import com.zik.faro.data.ActionStatus;
import com.zik.faro.data.Assignment;
import com.zik.faro.data.Event;
import com.zik.faro.data.Item;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssignmentLandingFragment extends Fragment{

    private static Event originalEvent;
    private static com.zik.faro.data.Activity originalActivity = null;
    String activityID = null;
    String eventID = null;
    String assignmentID = null;
    private static Assignment cloneAssignment = null;

    private static EventListHandler eventListHandler = EventListHandler.getInstance();
    private static ActivityListHandler activityListHandler = ActivityListHandler.getInstance();
    private static AssignmentListHandler assignmentListHandler = AssignmentListHandler.getInstance();
    private static FaroServiceHandler serviceHandler;

    private static String TAG = "AssgnmntLandingFrgmnt";

    private ListView itemList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.assignment_landing_fragment, container, false);
        super.onCreate(savedInstanceState);

        serviceHandler = eventListHandler.serviceHandler;

        Thread.setDefaultUncaughtExceptionHandler(new FaroExceptionHandler(getActivity()));

        eventID = getArguments().getString("eventID");
        activityID = getArguments().getString("activityID");
        assignmentID = getArguments().getString("assignmentID");

        final Context mContext = this.getActivity();

        cloneAssignment = assignmentListHandler.getAssignmentCloneFromMap(assignmentID);

        originalEvent = eventListHandler.getOriginalEventFromMap(eventID);
        final TextView assignmentDescription = (TextView) view.findViewById(R.id.assignmentDescription);
        if (activityID != null) {
            originalActivity = activityListHandler.getOriginalActivityFromMap(activityID);
            assignmentDescription.setText("Assignment for " + originalActivity.getName());
        } else {
            assignmentDescription.setText("Assignment for " + originalEvent.getEventName());
        }

        ImageButton editButton = (ImageButton) view.findViewById(R.id.editButton);
        editButton.setImageResource(R.drawable.edit);

        Button updateAssignment = (Button) view.findViewById(R.id.updateAssignment);

        final Intent EditAssignmentPage = new Intent(getActivity(), EditAssignment.class);
        final Intent AssignmentLandingPageReloadIntent = new Intent(getActivity(), AssignmentLandingPage.class);

        itemList = (ListView) view.findViewById(R.id.itemList);
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

                Map<String, List<Item>> itemListMap = new HashMap<String, List<Item>>();
                if (activityID != null) {
                    itemListMap.put(activityID, cloneItemsList);
                }else {
                    itemListMap.put(eventID, cloneItemsList);
                }

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
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditAssignmentPage.putExtra("eventID", eventID);
                EditAssignmentPage.putExtra("activityID", activityID);
                EditAssignmentPage.putExtra("assignmentID", assignmentID);
                startActivity(EditAssignmentPage);
                getActivity().finish();
            }
        });
        return view;
    }
}
