package com.zik.faro.frontend.faroservice.spec;


import com.zik.faro.frontend.data.Activity;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;

import java.util.List;

public interface ActivityHandler {
    void getActivities(final BaseFaroRequestCallback<List<ActivityHandler>> callback, final String eventId);
    void getActivity(final BaseFaroRequestCallback<ActivityHandler> callback, final String eventId, final String activityId);
    void createActivity(final BaseFaroRequestCallback<String> callback, final ActivityHandler activity);
    void updateActivity(final BaseFaroRequestCallback<Void> callback, 
                        final String eventId, 
                        final String activityId, 
                        final Activity.ActivityUpdateData updateData);
    void deleteActivity(final BaseFaroRequestCallback<Void> callback, final String eventId, final String activityId);
    
}
