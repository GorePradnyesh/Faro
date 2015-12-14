package com.zik.faro.frontend.faroservice.spec;

import com.zik.faro.data.MinUser;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;

import java.util.List;

public interface FriendsHandler {
    void inviteFriend(final BaseFaroRequestCallback<String> callback, final String userId);
    void getFriends(final BaseFaroRequestCallback<List<MinUser>> callback);
    void unFriend(final BaseFaroRequestCallback<String> callback, final String userId);
}
