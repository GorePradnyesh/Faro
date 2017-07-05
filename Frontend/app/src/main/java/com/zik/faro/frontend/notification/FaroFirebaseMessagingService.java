package com.zik.faro.frontend.notification;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


import org.json.JSONObject;

public class FaroFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String title = null;
        String body = null;
        String clickAction = null;
        JSONObject data = null;


        if (remoteMessage.getData().size() > 0){
            data = new JSONObject(remoteMessage.getData());
        }

        if (remoteMessage.getNotification() != null){
            title = remoteMessage.getNotification().getTitle();
            body = remoteMessage.getNotification().getBody();
            clickAction = remoteMessage.getNotification().getClickAction();
            Log.d(TAG, "title is " + title);
            Log.d(TAG, "body is" + body);
            Log.d(TAG, "clickAction type is " + clickAction);
        }
        sendNotification(title, body, clickAction, data);
    }

    private void sendNotification(String title, String messageBody, String notificationType, JSONObject data) {
        NotificationCompat.Builder notificationBuilder =
                FaroNotificationBuilder.getNotificationBuilder(this, title,
                        messageBody, notificationType, data);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
