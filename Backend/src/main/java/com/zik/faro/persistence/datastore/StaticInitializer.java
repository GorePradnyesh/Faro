package com.zik.faro.persistence.datastore;

import com.googlecode.objectify.ObjectifyService;
import com.zik.faro.data.Activity;
import com.zik.faro.data.EventDo;
import com.zik.faro.data.EventUser;
import com.zik.faro.data.Poll;
import com.zik.faro.data.user.FaroUser;
import com.zik.faro.data.user.FriendRelation;
import com.zik.faro.data.user.UserCredentials;

public class StaticInitializer {

    public static void init(){
        //ObjectifyService.register();
        ObjectifyService.register(Activity.class);
        ObjectifyService.register(EventDo.class);
        ObjectifyService.register(FaroUser.class);
        ObjectifyService.register(UserCredentials.class);
        ObjectifyService.register(EventUser.class);
        ObjectifyService.register(Poll.class);
        ObjectifyService.register(FriendRelation.class);
    }
}
