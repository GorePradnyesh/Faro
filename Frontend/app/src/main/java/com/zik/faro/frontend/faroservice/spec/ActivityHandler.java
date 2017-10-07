package com.zik.faro.frontend.faroservice.spec;


import com.zik.faro.data.Activity;
import com.zik.faro.data.Item;
import com.zik.faro.data.UpdateCollectionRequest;
import com.zik.faro.data.UpdateRequest;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;

import java.util.List;

public interface ActivityHandler {
    void getActivities(final BaseFaroRequestCallback<List<Activity>> callback, final String eventId);
    void getActivity(final BaseFaroRequestCallback<Activity> callback, final String eventId, final String activityId);
    void createActivity(final BaseFaroRequestCallback<Activity> callback, final String eventId, final Activity activity);
    void updateActivity(final BaseFaroRequestCallback<String> callback, 
                        final String eventId, 
                        final String activityId, 
                        final UpdateRequest<Activity> updateRequest);
    void updateActivityAssignment(final BaseFaroRequestCallback<Activity> callback,
                                  final String eventId,
                                  final String activityId,
                                  final UpdateCollectionRequest<Activity, Item> updateCollectionRequest);
    void deleteActivity(final BaseFaroRequestCallback<String> callback, final String eventId, final String activityId);
    
}
