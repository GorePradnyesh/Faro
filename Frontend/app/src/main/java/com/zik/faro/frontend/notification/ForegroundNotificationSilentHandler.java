package com.zik.faro.frontend.notification;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.squareup.okhttp.Request;
import com.zik.faro.data.Event;
import com.zik.faro.data.EventInviteStatusWrapper;
import com.zik.faro.frontend.EventListHandler;
import com.zik.faro.frontend.FaroIntentConstants;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.FaroServiceHandler;
import com.zik.faro.frontend.faroservice.HttpError;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ForegroundNotificationSilentHandler {
    private String faroNotificationDataStr = null;
    private JSONObject faroNotificationDataJSON = null;
    private String payloadNotificationType = "default";
    private String eventId = null;
    private String pollId = null;
    private String assignmentId = null;
    private String activityId = null;
    private String TAG = "FrgndNtfnSilentHndlr";

    private FaroServiceHandler serviceHandler = FaroServiceHandler.getFaroServiceHandler();
    private EventListHandler eventListHandler = EventListHandler.getInstance();

    public void updateGlobalMemoryIfPresent(Context context, JSONObject notificationDataJSON) throws JSONException {
        faroNotificationDataStr = notificationDataJSON.getString(FaroIntentConstants.FARO_NOTIFICATION_DATA);
        faroNotificationDataJSON = new JSONObject(faroNotificationDataStr);
        payloadNotificationType = faroNotificationDataJSON.getString(FaroIntentConstants.PAYLOAD_NOTIFICATION_TYPE);

        switch (payloadNotificationType){
            case "notificationType_EventInvite":
            case "notificationType_EventDateChanged":
            case "notificationType_EventTimeChanged":
            case "notificationType_EventLocationChanged":
            case "notificationType_EventDescriptionUpdate":
            case "notificationType_EventGeneric":
                updateEventObjectInGlobalMemory(context, faroNotificationDataJSON);
                break;
            case "notificationType_PollCreated":
            case "notificationType_PollModified":
            case "notificationType_PollClosed":
            case "notificationType_PollGeneric":

                break;
            case "notificationType_ActivityCreated":
            case "notificationType_ActivityDateChanged":
            case "notificationType_ActivityTimeChanged":
            case "notificationType_ActivityLocationChanged":
            case "notificationType_ActivityDescriptionChanged":
            case "notificationType_ActivityGeneric":
                break;
            case "notificationType_NewAssignmentForYou":
            case "notificationType_AssignmentCreated":
            case "notificationType_AssignmentPending":
                break;
            case "notificationType_FriendInviteAccepted":
            case "notificationType_FriendInviteMaybe":
            case "notificationType_FriendInviteNotGoing":
                break;
            case "notificationType_RemovedFromEvent":
            case "notificationType_EventDeleted":
            case "notificationType_ActivityDeleted":
            case "notificationType_PollDeleted":
            default:
                break;
        }
    }

    private void updateEventObjectInGlobalMemory(Context context, JSONObject faroNotificationDataJSON) throws JSONException {
        eventId = faroNotificationDataJSON.getString(FaroIntentConstants.EVENT_ID);

        //Check if event object present in global memory for this eventId.
        Event event = eventListHandler.getOriginalEventFromMap(eventId);
        if (event == null)
            return;

        // If version passed in the notification, then check it against the version in the event object
        // If greater only then make an api call.
        getEventFromServer(context);
    }

    private void getEventFromServer(final Context context){
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
                    //TODO: Is it ok to not have the below code in a seperate thread in this case?
                    Log.i(TAG, "Successfully received Event from server");
                    Event event = eventInviteStatusWrapper.getEvent();
                    eventListHandler.addEventToListAndMap(event,
                            eventInviteStatusWrapper.getInviteStatus());
                        }
                    };
                    Handler mainHandler = new Handler(context.getMainLooper());
                    mainHandler.post(myRunnable);
                }else {
                    Log.i(TAG, "code = " + error.getCode() + ", message = " + error.getMessage());
                }
            }
        }, eventId);
    }
}
