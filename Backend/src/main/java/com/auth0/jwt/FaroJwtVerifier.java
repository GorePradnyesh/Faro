package com.auth0.jwt;

import com.auth0.jwt.internal.com.fasterxml.jackson.databind.JsonNode;
import com.auth0.jwt.internal.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Map;

/**
 * Created by granganathan on 4/19/15.
 */
public class FaroJwtVerifier extends JWTVerifier {

    private final Base64 decoder = new Base64(true);
    final String secretString;

    public FaroJwtVerifier(String secret, String audience, String issuer) {
        super(secret, audience, issuer);
        this.secretString = secret;
    }

    public FaroJwtVerifier(String secret, String audience) {
        super(secret, audience);
        this.secretString = secret;
    }

    public FaroJwtVerifier(String secret) {
        super(secret);
        this.secretString = secret;
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

    @Override
    void verifySignature(String[] pieces, String algorithm) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Mac hmac = Mac.getInstance(algorithm);
        hmac.init(new SecretKeySpec(this.secretString.getBytes(), algorithm));
        byte[] sig = hmac.doFinal(new StringBuilder(pieces[0]).append(".").append(pieces[1]).toString().getBytes());
        // System.out.println(""+ bytesToHex(sig) + "\n" + bytesToHex(decoder.decodeBase64(pieces[2])));
        if (!MessageDigest.isEqual(sig, decoder.decodeBase64(pieces[2]))) {
            throw new SignatureException("signature verification failed");
        }
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

}
