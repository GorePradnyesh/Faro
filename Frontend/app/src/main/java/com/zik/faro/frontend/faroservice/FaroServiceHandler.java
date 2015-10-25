package com.zik.faro.frontend.faroservice;

import com.zik.faro.frontend.faroservice.okHttp.OKHttpWrapperActivity;
import com.zik.faro.frontend.faroservice.okHttp.OKHttpWrapperEvent;
import com.zik.faro.frontend.faroservice.okHttp.OKHttpWrapperFriends;
import com.zik.faro.frontend.faroservice.okHttp.OKHttpWrapperPoll;
import com.zik.faro.frontend.faroservice.okHttp.OKHttpWrapperSignup;
import com.zik.faro.frontend.faroservice.okHttp.OkHttpWrapperProfile;
import com.zik.faro.frontend.faroservice.spec.ActivityHandler;
import com.zik.faro.frontend.faroservice.spec.EventHandler;
import com.zik.faro.frontend.faroservice.spec.FriendsHandler;
import com.zik.faro.frontend.faroservice.spec.PollHandler;
import com.zik.faro.frontend.faroservice.spec.ProfileHandler;
import com.zik.faro.frontend.faroservice.spec.SignupHandler;

import java.net.URL;

public class FaroServiceHandler {
    private EventHandler        eventHandler;
    private ProfileHandler      profileHandler;
    private SignupHandler       signupHandler;
    private ActivityHandler     activityHandler;
    private PollHandler         pollHandler;
    private FriendsHandler      friendsHandler;

    private FaroServiceHandler(){}

    public static FaroServiceHandler getFaroServiceHandler(final URL baseUrl){
        FaroServiceHandler serviceHandler = new FaroServiceHandler();
        serviceHandler.eventHandler = new OKHttpWrapperEvent(baseUrl);
        serviceHandler.profileHandler = new OkHttpWrapperProfile(baseUrl);
        serviceHandler.signupHandler = new OKHttpWrapperSignup(baseUrl);
        serviceHandler.pollHandler = new OKHttpWrapperPoll(baseUrl);
        serviceHandler.activityHandler = new OKHttpWrapperActivity(baseUrl);
        serviceHandler.friendsHandler = new OKHttpWrapperFriends(baseUrl);
        return serviceHandler;
    }

    public EventHandler getEventHandler() {
        return eventHandler;
    }

    public ProfileHandler getProfileHandler() {
        return profileHandler;
    }

    public SignupHandler getSignupHandler() {
        return signupHandler;
    }
    
    public PollHandler getPollHandler() {
        return pollHandler;
    }

    public ActivityHandler getActivityHandler() {
        return activityHandler;
    }

    public FriendsHandler getFriendsHandler() {
        return friendsHandler;
    }
}
