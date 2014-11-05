package com.zik.faro.api.profile;


import com.zik.faro.commons.Constants;
import com.zik.faro.data.user.Address;
import com.zik.faro.data.user.FaroUser;
import com.zik.faro.data.user.FaroUserName;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("profile")
public class ProfileHandler {
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public FaroUser getProfile(@QueryParam(Constants.SIGNATURE_QUERY_PARAM) final String signature){
        return new FaroUser("rwaters@gmail.com",
                new FaroUserName("Roger","wAters"),
                "rwaters@splitwise.com",
                "4085393212",
                new Address(44, "Abby Road","SouthEnd London","UK", 566645));
    }

    @PUT
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public String getProfileJson(FaroUser faroUser){
        return faroUser.userName.firstName;
    }
}
