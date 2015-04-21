package com.auth0.jwt;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.internal.com.fasterxml.jackson.databind.JsonNode;
import com.auth0.jwt.internal.com.fasterxml.jackson.databind.ObjectMapper;
import com.zik.faro.auth.jwt.FaroJwtClaims;

import java.io.IOException;
import java.util.Map;

/**
 * Created by granganathan on 4/19/15.
 */
public class FaroJwtVerifier extends JWTVerifier {

    public FaroJwtVerifier(String secret, String audience, String issuer) {
        super(secret, audience, issuer);
    }

    public FaroJwtVerifier(String secret, String audience) {
        super(secret, audience);
    }

    public FaroJwtVerifier(String secret) {
        super(secret);
    }

    public Map<String, Object> obtainJwtClaimsWithoutValidations(String token) throws IOException {
        if (token == null || "".equals(token)) {
            throw new IllegalStateException("token not set");
        }

        String[] pieces = token.split("\\.");

        // check number of segments
        if (pieces.length != 3) {
            throw new IllegalStateException("Wrong number of segments: " + pieces.length);
        }

        // get JWTHeader JSON object. Extract algorithm
        JsonNode jwtHeader = decodeAndParse(pieces[0]);
        String algorithm = getAlgorithm(jwtHeader);

        // get JWTClaims JSON object
        JsonNode jwtPayload = decodeAndParse(pieces[1]);

        ObjectMapper mapper = new ObjectMapper();
        return mapper.treeToValue(jwtPayload, Map.class);
    }
}
