package com.zik.faro.frontend.faroservice.auth;

public final class JwtClaimConstants {
    /**** Registered Claim Names ****/

    // The "iss" (issuer) claim identifies the principal that issued the JWT
    public static final String ISSUER_KEY = "iss";

    // The "sub" (subject) claim identifies the principal that is the subject of the JWT.
    // The Claims in a JWT are normally statements about the subject.
    public static final String SUBJECT_KEY = "sub";

    // The "aud" (audience) claim identifies the recipients that the JWT is
    // intended for.  Each principal intended to process the JWT MUST
    // identify itself with a value in the audience claim.  If the principal
    // processing the claim does not identify itself with a value in the
    // "aud" claim when this claim is present, then the JWT MUST b rejected.
    public static final String AUDIENCE_KEY = "aud";

    // The "iat" (issued at) claim identifies the time at which the JWT was
    // issued.  This claim can be used to determine the age of the JWT.
    public static final String ISSUED_AT_KEY = "iat";

    // The "exp" (expiration time) claim identifies the expiration time on
    // or after which the JWT MUST NOT be accepted for processing.
    public static final String EXPIRATION_KEY = "exp";

    /**** Private claim names ****/

    // Faro username of the user
    public static final String USERNAME = "username";

    // Email address of the user
    public static final String EMAIL = "email";
}