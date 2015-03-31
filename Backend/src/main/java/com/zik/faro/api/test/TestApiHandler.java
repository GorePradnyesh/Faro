package com.zik.faro.api.test;

import com.zik.faro.auth.FaroPrincipal;
import com.zik.faro.auth.jwt.JwtClaims;
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

        JwtClaims jwtClaims = ((FaroPrincipal)securityContext.getUserPrincipal()).getJwtClaims();
        logger.info("jwtclaims: " + jwtClaims);

        return Response.ok().build();
    }
 }
