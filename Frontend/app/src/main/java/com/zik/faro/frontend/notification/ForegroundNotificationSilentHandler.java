package com.zik.faro.frontend.notification;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.squareup.okhttp.Request;
import com.zik.faro.data.Activity;
import com.zik.faro.data.Assignment;
import com.zik.faro.data.Event;
import com.zik.faro.data.EventInviteStatusWrapper;
import com.zik.faro.data.InviteeList;
import com.zik.faro.data.Poll;
import com.zik.faro.frontend.ActivityListHandler;
import com.zik.faro.frontend.AssignmentListHandler;
import com.zik.faro.frontend.EventFriendListHandler;
import com.zik.faro.frontend.EventListHandler;
import com.zik.faro.frontend.FaroIntentConstants;
import com.zik.faro.frontend.PollListHandler;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class ForegroundNotificationSilentHandler {
    private String faroNotificationDataStr = null;
    private JSONObject faroNotificationDataJSON = null;
    private String payloadNotificationType = "default";
    private String eventId = null;
    private String pollId = null;
    private String assignmentId = null;
    private String activityId = null;
    private String TAG = "FrgndNtfnSilentHndlr";
    private Context mContext = null;

    private FaroServiceHandler serviceHandler = FaroServiceHandler.getFaroServiceHandler();
    private EventListHandler eventListHandler = EventListHandler.getInstance();
    private PollListHandler pollListHandler = PollListHandler.getInstance();
    private ActivityListHandler activityListHandler = ActivityListHandler.getInstance();
    private AssignmentListHandler assignmentListHandler = AssignmentListHandler.getInstance();
    private EventFriendListHandler eventFriendListHandler = EventFriendListHandler.getInstance();

    public void updateGlobalMemoryIfPresent(Context context, JSONObject notificationDataJSON) throws JSONException {
        faroNotificationDataStr = notificationDataJSON.getString(FaroIntentConstants.FARO_NOTIFICATION_DATA);
        faroNotificationDataJSON = new JSONObject(faroNotificationDataStr);
        payloadNotificationType = faroNotificationDataJSON.getString(FaroIntentConstants.PAYLOAD_NOTIFICATION_TYPE);
        mContext = context;

        switch (payloadNotificationType){
            case "notificationType_EventInvite":
            case "notificationType_EventDateChanged":
            case "notificationType_EventTimeChanged":
            case "notificationType_EventLocationChanged":
            case "notificationType_EventDescriptionUpdate":
            case "notificationType_EventGeneric":
                updateEventObjectInGlobalMemory();
                break;
            case "notificationType_PollCreated":
            case "notificationType_PollModified":
            case "notificationType_PollClosed":
            case "notificationType_PollGeneric":
                updatePollObjectInGlobalMemory();
                break;
            case "notificationType_ActivityCreated":
            case "notificationType_ActivityDateChanged":
            case "notificationType_ActivityTimeChanged":
            case "notificationType_ActivityLocationChanged":
            case "notificationType_ActivityDescriptionChanged":
            case "notificationType_ActivityGeneric":
                updateActivityObjectInGlobalMemory();
                break;
            case "notificationType_NewAssignmentForYou":
            case "notificationType_AssignmentCreated":
            case "notificationType_AssignmentPending":
                updateAssignmentObjectInGlobalMemory();
                break;
            case "notificationType_FriendInviteAccepted":
            case "notificationType_FriendInviteMaybe":
            case "notificationType_FriendInviteNotGoing":
                updateEventFriendListInGlobalMemory();
                break;
            case "notificationType_RemovedFromEvent":
            case "notificationType_EventDeleted":
            case "notificationType_ActivityDeleted":
            case "notificationType_PollDeleted":
            default:
                break;
        }
    }

    private void updateEventObjectInGlobalMemory() throws JSONException {
        eventId = faroNotificationDataJSON.getString(FaroIntentConstants.EVENT_ID);

        //Check if event object present in global memory for this eventId.
        Event event = eventListHandler.getOriginalEventFromMap(eventId);
        if (event == null)
            return;

        // If version passed in the notification, then check it against the version in the event object
        // If greater only then make an api call.
        getEventFromServer();
    }

    private void updatePollObjectInGlobalMemory() throws JSONException {
        eventId = faroNotificationDataJSON.getString(FaroIntentConstants.EVENT_ID);
        pollId = faroNotificationDataJSON.getString(FaroIntentConstants.POLL_ID);

        //Check if poll object is present in global memory.
        Poll poll = pollListHandler.getOriginalPollFromMap(pollId);
        if (poll == null)
            return;

        // If version passed in the notification, then check it against the version in the poll object
        // If greater only then make an api call.
        getUpdatedPollFromServer();
    }

    private void updateActivityObjectInGlobalMemory() throws JSONException {
        eventId = faroNotificationDataJSON.getString(FaroIntentConstants.EVENT_ID);
        activityId = faroNotificationDataJSON.getString(FaroIntentConstants.ACTIVITY_ID);

        //Check if Activity object is present in global memory.
        Activity activity = activityListHandler.getOriginalActivityFromMap(activityId);
        if (activity == null)
            return;

        // If version passed in the notification, then check it against the version in the activity object
        // If greater only then make an api call.
        getUpdatedActivityFromServer();
    }

    private void updateAssignmentObjectInGlobalMemory() throws JSONException {
        eventId = faroNotificationDataJSON.getString(FaroIntentConstants.EVENT_ID);
        activityId = faroNotificationDataJSON.getString(FaroIntentConstants.ACTIVITY_ID);
        assignmentId = faroNotificationDataJSON.getString(FaroIntentConstants.ASSIGNMENT_ID);

        //Check if Assignment object is present in global memory.
        Assignment assignment =  assignmentListHandler.getOriginalAssignmentFromMap(assignmentId);
        if (assignment == null)
            return;

        // If version passed in the notification, then check it against the version in the activity object
        // If greater only then make an api call.
        getUpdatedAssignmentFromServer();
    }

    private void updateEventFriendListInGlobalMemory() throws JSONException {
        eventId = faroNotificationDataJSON.getString(FaroIntentConstants.EVENT_ID);

        //Check if event object present in global memory for this eventId.
        Event event = eventListHandler.getOriginalEventFromMap(eventId);
        if (event == null)
            return;

        getEventInviteesFromServer();
    }

    private void getEventFromServer(){
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
                    Event event = eventInviteStatusWrapper.getEvent();
                    eventListHandler.addEventToListAndMap(event,
                            eventInviteStatusWrapper.getInviteStatus());
                        }
                    };
                    Handler mainHandler = new Handler(mContext.getMainLooper());
                    mainHandler.post(myRunnable);
                }else {
                    Log.i(TAG, "code = " + error.getCode() + ", message = " + error.getMessage());
                }
            }
        }, eventId);
    }

    private void getUpdatedPollFromServer(){
        serviceHandler.getPollHandler().getPoll(new BaseFaroRequestCallback<Poll>() {
            @Override
            public void onFailure(Request request, IOException ex) {
                Log.e(TAG, "failed to get the updated Poll Object");
            }

            @Override
            public void onResponse(final Poll receivedPoll, HttpError error) {
                if (error == null ) {
                    //Since update to server successful
                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            pollListHandler.addPollToListAndMap(eventId, receivedPoll, mContext);
                        }
                    };
                    Handler mainHandler = new Handler(mContext.getMainLooper());
                    mainHandler.post(myRunnable);
                }
                else {
                    Log.i(TAG, "code = " + error.getCode() + ", message = " + error.getMessage());
                }

            }
        }, eventId, pollId);
    }

    private void getUpdatedActivityFromServer() {
        serviceHandler.getActivityHandler().getActivity(new BaseFaroRequestCallback<Activity>() {
            @Override
            public void onFailure(Request request, IOException ex) {
                Log.e(TAG, "failed to get activity list");
            }

            @Override
            public void onResponse(final Activity activity, HttpError error) {
                if (error == null) {
                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "Successfully received activities from the server!!");
                            activityListHandler.addActivityToListAndMap(eventId, activity, mContext);
                        }
                    };
                    Handler mainHandler = new Handler(mContext.getMainLooper());
                    mainHandler.post(myRunnable);
                } else {
                    Log.i(TAG, "code = " + error.getCode() + ", message = " + error.getMessage());
                }
            }
        }, eventId, activityId);
    }

    private void getUpdatedAssignmentFromServer() {
        serviceHandler.getAssignmentHandler().getAssignment(new BaseFaroRequestCallback<Assignment>() {
            @Override
            public void onFailure(Request request, IOException ex) {
                Log.e(TAG, "failed to get Event from server");
            }

            @Override
            public void onResponse(final Assignment assignment, HttpError error) {
                if (error == null ) {
                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "Successfully received Assignment from server");
                            assignmentListHandler.addAssignmentToListAndMap(eventId,
                                    assignment, activityId, mContext);
                        }
                    };
                    Handler mainHandler = new Handler(mContext.getMainLooper());
                    mainHandler.post(myRunnable);
                }else {
                    Log.i(TAG, "code = " + error.getCode() + ", message = " + error.getMessage());
                }
            }
        }, eventId, assignmentId, activityId);
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
                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "Successfully received Invitee List for the cloneEvent");
                            eventFriendListHandler.addDownloadedFriendsToListAndMap(eventId, inviteeList, mContext);
                        }
                    };
                    Handler mainHandler = new Handler(mContext.getMainLooper());
                    mainHandler.post(myRunnable);
                } else {
                    Log.i(TAG, "code = " + error.getCode() + ", message = " + error.getMessage());
                }
            }
        }, eventId);
    }
}
