package com.zik.faro.api.profile;


import com.zik.faro.data.user.Address;
import com.zik.faro.data.user.FaroUser;
import com.zik.faro.data.user.FaroUserName;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("profile")
public class ProfileHandler {
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getProfile(){
        return "Hello World";
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public FaroUser getProfileHtml(){
        return new FaroUser("rwaters@gmail.com",
                new FaroUserName("Roger","wAters"),
                "rwaters@splitwise.com",
                "4085393212",
                new Address(44, "Abby Road","SouthEnd London","UK", 566645));
    }
}
