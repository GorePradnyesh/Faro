package com.zik.faro.frontend.faroservice.notification;

// Implement this interface on all Activities which would be opened directly through a notification
public interface NotificationPayloadHandler {
    void checkAndHandleNotification();
}
