package com.zik.faro.persistence.datastore;

import com.googlecode.objectify.ObjectifyService;
import com.zik.faro.data.Activity;
import com.zik.faro.data.Event;
import com.zik.faro.data.user.FaroUser;
import com.zik.faro.data.user.UserCredentials;

public class StaticInitializer {

    public static void init(){
        //ObjectifyService.register();
        ObjectifyService.register(Activity.class);
        ObjectifyService.register(Event.class);
        ObjectifyService.register(FaroUser.class);
        ObjectifyService.register(UserCredentials.class);
    }
}
