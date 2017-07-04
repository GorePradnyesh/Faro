package com.zik.faro.frontend.util;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;

import com.zik.faro.frontend.FaroIntentConstants;

public class FaroIntentInfoBuilder {

    static private void setNotificationInfo (Intent intent, boolean isNotification){
        if (isNotification)
            intent.putExtra(FaroIntentConstants.BUNDLE_TYPE, FaroIntentConstants.IS_NOTIFICATION);
        else
            intent.putExtra(FaroIntentConstants.BUNDLE_TYPE, FaroIntentConstants.IS_NOT_NOTIFICATION);
    }

    public static void eventIntent(Intent intent, String eventId, boolean isNotification) {
        intent.putExtra(FaroIntentConstants.EVENT_ID, eventId);
        setNotificationInfo(intent, isNotification);
    }

    public static void eventIntent(Intent intent, String eventId) {
        intent.putExtra(FaroIntentConstants.EVENT_ID, eventId);
        setNotificationInfo(intent, false);
    }

    public static void activityIntent (Intent intent, String eventId, String activityId,
                                       boolean isNotification) {
        intent.putExtra(FaroIntentConstants.EVENT_ID, eventId);
        intent.putExtra(FaroIntentConstants.ACTIVITY_ID, activityId);
        setNotificationInfo(intent, isNotification);
    }

    public static void activityIntent (Intent intent, String eventId, String activityId) {
        intent.putExtra(FaroIntentConstants.EVENT_ID, eventId);
        intent.putExtra(FaroIntentConstants.ACTIVITY_ID, activityId);
        setNotificationInfo(intent, false);
    }

    public static void assignmentIntent (Intent intent, String eventId, String activityId,
                                         String assignmentId, boolean isNotification) {
        intent.putExtra(FaroIntentConstants.EVENT_ID, eventId);
        intent.putExtra(FaroIntentConstants.ACTIVITY_ID, activityId);
        intent.putExtra(FaroIntentConstants.ASSIGNMENT_ID, assignmentId);
        setNotificationInfo(intent, isNotification);
    }

    public static void assignmentIntent (Intent intent, String eventId, String activityId, String assignmentId) {
        intent.putExtra(FaroIntentConstants.EVENT_ID, eventId);
        intent.putExtra(FaroIntentConstants.ACTIVITY_ID, activityId);
        intent.putExtra(FaroIntentConstants.ASSIGNMENT_ID, assignmentId);
        setNotificationInfo(intent, false);
    }

    public static void pollIntent (Intent intent, String eventId, String pollId, boolean isNotification) {
        intent.putExtra(FaroIntentConstants.EVENT_ID, eventId);
        intent.putExtra(FaroIntentConstants.POLL_ID, pollId);
        setNotificationInfo(intent, isNotification);
    }

    public static void pollIntent (Intent intent, String eventId, String pollId) {
        intent.putExtra(FaroIntentConstants.EVENT_ID, eventId);
        intent.putExtra(FaroIntentConstants.POLL_ID, pollId);
        setNotificationInfo(intent, false);
    }

    public static void userProfileIntent (Intent intent, String emailId,
                                          String eventId) {
        intent.putExtra(FaroIntentConstants.EVENT_ID, eventId);
        intent.putExtra(FaroIntentConstants.EMAIL_ID, emailId);
        setNotificationInfo(intent, false);
    }

    public static void userProfileIntent (Intent intent, String emailId,
                                          String eventId, String listStatus, boolean isNotification) {
        intent.putExtra(FaroIntentConstants.EVENT_ID, eventId);
        intent.putExtra(FaroIntentConstants.EMAIL_ID, emailId);
        intent.putExtra(FaroIntentConstants.LIST_STATUS, listStatus);
        setNotificationInfo(intent, isNotification);
    }

    public static void pictureIntent (Intent intent, Uri photoURI) {
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        setNotificationInfo(intent, false);
    }

    public static void pictureIntent (Intent intent, Uri photoURI, boolean isNotification) {
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        setNotificationInfo(intent, isNotification);
    }

    public static void notificationHandlerIntent (Intent intent, String notificationType, String notificationData) {
        intent.putExtra(FaroIntentConstants.NOTIFICATION_TYPE, notificationType);
        intent.putExtra(FaroIntentConstants.NOTIFICATION_DATA, notificationData);
    }
}
