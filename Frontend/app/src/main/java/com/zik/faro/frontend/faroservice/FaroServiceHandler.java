package com.zik.faro.frontend.faroservice;

import com.zik.faro.frontend.faroservice.okHttp.OKHttpWrapperActivity;
import com.zik.faro.frontend.faroservice.okHttp.OKHttpWrapperEvent;
import com.zik.faro.frontend.faroservice.okHttp.OKHttpWrapperFriends;
import com.zik.faro.frontend.faroservice.okHttp.OKHttpWrapperLogin;
import com.zik.faro.frontend.faroservice.okHttp.OKHttpWrapperPoll;
import com.zik.faro.frontend.faroservice.okHttp.OKHttpWrapperSignup;
import com.zik.faro.frontend.faroservice.okHttp.OkHttpWrapperAssignment;
import com.zik.faro.frontend.faroservice.okHttp.OkHttpWrapperProfile;
import com.zik.faro.frontend.faroservice.spec.ActivityHandler;
import com.zik.faro.frontend.faroservice.spec.AssignmentHandler;
import com.zik.faro.frontend.faroservice.spec.EventHandler;
import com.zik.faro.frontend.faroservice.spec.FriendsHandler;
import com.zik.faro.frontend.faroservice.spec.LoginHandler;
import com.zik.faro.frontend.faroservice.spec.PollHandler;
import com.zik.faro.frontend.faroservice.spec.ProfileHandler;
import com.zik.faro.frontend.faroservice.spec.SignupHandler;

import java.net.URL;

public class FaroServiceHandler {
    private static  FaroServiceHandler serviceHandler;
    private EventHandler        eventHandler;
    private ProfileHandler      profileHandler;
    private SignupHandler       signupHandler;
    private LoginHandler        loginHandler;
    private ActivityHandler     activityHandler;
    private PollHandler         pollHandler;
    private FriendsHandler      friendsHandler;
    private AssignmentHandler   assignmentHandler;

    private FaroServiceHandler(URL baseUrl) {
        eventHandler = new OKHttpWrapperEvent(baseUrl);
        profileHandler = new OkHttpWrapperProfile(baseUrl);
        signupHandler = new OKHttpWrapperSignup(baseUrl);
        loginHandler = new OKHttpWrapperLogin(baseUrl);
        pollHandler = new OKHttpWrapperPoll(baseUrl);
        activityHandler = new OKHttpWrapperActivity(baseUrl);
        friendsHandler = new OKHttpWrapperFriends(baseUrl);
        assignmentHandler = new OkHttpWrapperAssignment(baseUrl);
    }

    public static FaroServiceHandler initializeInstance(URL baseUrl) {
        serviceHandler = new FaroServiceHandler(baseUrl);
        return serviceHandler;
    }

    public static FaroServiceHandler getFaroServiceHandler() {
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

    public LoginHandler getLoginHandler() {
        return loginHandler;
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

    public AssignmentHandler getAssignmentHandler() {
        return assignmentHandler;
    }
}
