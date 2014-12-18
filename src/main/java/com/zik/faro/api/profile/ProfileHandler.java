package com.zik.faro.api.profile;


import static com.zik.faro.commons.Constants.*;
import com.zik.faro.commons.ParamValidation;
import com.zik.faro.data.user.Address;
import com.zik.faro.data.user.FaroUser;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path(PROFILE_PATH_CONST)
public class ProfileHandler {
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public FaroUser getProfile(@QueryParam(SIGNATURE_QUERY_PARAM) final String signature){
        ParamValidation.validateSignature(signature);
        // TODO: Wrap user within profile object, used for responder
        return new FaroUser("rwaters@gmail.com",
                "Roger", null, "waters",
                "rwaters@splitwise.com",
                "4085393212",
                new Address(44, "Abby Road","SouthEnd London","UK", 566645));
    }

    @PUT
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public String getProfileJson(FaroUser faroUser, @QueryParam(SIGNATURE_QUERY_PARAM) final String signature){
        // TODO:  Add param validation
        ParamValidation.validateSignature(signature);
        return faroUser.getFirstName();
    }
}
