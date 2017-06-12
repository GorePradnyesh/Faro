package com.zik.faro.frontend.notification;

// Implement this interface on all Activities which would be opened directly through a notification
public interface NotificationPayloadHandler {
    void checkAndHandleNotification();
}
