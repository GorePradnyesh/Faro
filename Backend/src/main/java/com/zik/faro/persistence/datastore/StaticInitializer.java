package com.zik.faro.persistence.datastore;

import java.io.IOException;

import com.googlecode.objectify.ObjectifyService;
import com.zik.faro.commons.ConfigPropertiesUtil;
import com.zik.faro.persistence.datastore.data.ActivityDo;
import com.zik.faro.persistence.datastore.data.EventDo;
import com.zik.faro.persistence.datastore.data.EventUserDo;
import com.zik.faro.persistence.datastore.data.FaroImageDo;
import com.zik.faro.persistence.datastore.data.GroupDo;
import com.zik.faro.persistence.datastore.data.GroupUserDo;
import com.zik.faro.persistence.datastore.data.PollDo;
import com.zik.faro.persistence.datastore.data.user.FaroUserDo;
import com.zik.faro.persistence.datastore.data.user.FriendRelationDo;
import com.zik.faro.persistence.datastore.data.user.UserCredentialsDo;

public class StaticInitializer {

    public static void init() throws IOException {
        // Objectify service registrations
        ObjectifyService.register(ActivityDo.class);
        ObjectifyService.register(EventDo.class);
        ObjectifyService.register(FaroUserDo.class);
        ObjectifyService.register(UserCredentialsDo.class);
        ObjectifyService.register(EventUserDo.class);
        ObjectifyService.register(PollDo.class);
        ObjectifyService.register(FriendRelationDo.class);
        ObjectifyService.register(FaroImageDo.class);
        ObjectifyService.register(GroupDo.class);
        ObjectifyService.register(GroupUserDo.class);

        // Load app server props from config file
        ConfigPropertiesUtil.loadPropertiesFile();
    }
}
