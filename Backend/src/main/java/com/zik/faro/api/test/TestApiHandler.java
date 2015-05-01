package com.zik.faro.api.test;

import com.zik.faro.auth.jwt.FaroJwtClaims;
import com.zik.faro.mail.FaroEmailClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import static com.zik.faro.commons.Constants.*;

/**
 * Created by granganathan on 3/24/15.
 */
@Path(TESTS_API_PATH_CONST)
public class TestApiHandler {
    private static Logger logger = LoggerFactory.getLogger(TestApiHandler.class);
    @Context
    SecurityContext securityContext;

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response testApi() {
        logger.info("Test API Invoked .........");

        FaroJwtClaims jwtClaims = (FaroJwtClaims)securityContext.getUserPrincipal();
        logger.info("jwtclaims: " + jwtClaims);
        logger.info("Sending email .....");
        FaroEmailClient emailClient = new FaroEmailClient();
        emailClient.sendEmail();

        return Response.ok().build();
    }
 }
