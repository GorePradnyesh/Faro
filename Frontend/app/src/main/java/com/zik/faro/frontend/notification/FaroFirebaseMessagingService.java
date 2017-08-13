package com.zik.faro.frontend.notification;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.zik.faro.frontend.FaroIntentConstants;


import org.json.JSONException;
import org.json.JSONObject;

public class FaroFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String title = "";
        String body = "";
        String clickAction = "";
        String notificationId = "";
        JSONObject data = null;
        String faroNotificationDataStr = null;
        JSONObject faroNotificationDataJSON = null;

        if (remoteMessage.getNotification() != null){
            title = remoteMessage.getNotification().getTitle();
            body = remoteMessage.getNotification().getBody();
            clickAction = remoteMessage.getNotification().getClickAction();
            Log.d(TAG, "title is " + title);
            Log.d(TAG, "body is" + body);
            Log.d(TAG, "clickAction type is " + clickAction);
        }

        if (remoteMessage.getData().size() > 0){
            data = new JSONObject(remoteMessage.getData());
            try {
                faroNotificationDataStr = data.getString(FaroIntentConstants.FARO_NOTIFICATION_DATA);
                faroNotificationDataJSON = new JSONObject(faroNotificationDataStr);
                title = faroNotificationDataJSON.getString(FaroIntentConstants.TITLE);
                body = faroNotificationDataJSON.getString(FaroIntentConstants.BODY);
                clickAction = faroNotificationDataJSON.getString(FaroIntentConstants.CLICK_ACTION);
                notificationId = faroNotificationDataJSON.getString(FaroIntentConstants.NOTIFICATION_ID);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        sendNotification(title, body, clickAction, data, notificationId);
    }

    private void sendNotification(String title, String messageBody, String clickAction,
                                  JSONObject data, String notificationId) {
        NotificationCompat.Builder notificationBuilder =
                FaroNotificationBuilder.getNotificationBuilder(this, title,
                        messageBody, clickAction, data);

        if (notificationBuilder == null)
            return;

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(notificationId, 0, notificationBuilder.build());
    }
}
