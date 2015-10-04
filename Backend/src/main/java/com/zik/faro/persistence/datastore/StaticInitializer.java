package com.zik.faro.persistence.datastore;

import com.googlecode.objectify.ObjectifyService;
import com.zik.faro.data.*;
import com.zik.faro.data.user.FaroUser;
import com.zik.faro.data.user.UserCredentials;

public class StaticInitializer {

    public static void init(){
        //ObjectifyService.register();
        ObjectifyService.register(Activity.class);
        ObjectifyService.register(Event.class);
        ObjectifyService.register(FaroUser.class);
        ObjectifyService.register(Poll.class);
        ObjectifyService.register(UserCredentials.class);
        ObjectifyService.register(EventUser.class);
    }
}
