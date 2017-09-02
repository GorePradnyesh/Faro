package com.zik.faro.frontend.data;

import com.zik.faro.data.MinUser;

/**
 * Created by gaurav on 8/5/17.
 */

public class FacebookMinUser extends MinUser {
    private String facebookUserId;

    public FacebookMinUser(String facebookUserId) {
        super();
        this.facebookUserId = facebookUserId;
    }

    public String getFacebookUserId() {
        return facebookUserId;
    }
}
