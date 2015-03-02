package com.zik.faro.api.authentication;

import com.auth0.jwt.internal.org.apache.commons.codec.binary.Hex;
import com.google.appengine.repackaged.com.google.common.base.Strings;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

/**
 * Created by granganathan on 2/15/15.
 */
public class PasswordManager {
    private static final String PASSWORD_FIXED_SALT = "iamthenight$";
    private static final String HASH_ALGORITHM = "SHA";
    private static final int HASH_NUM_ITERATIONS = 1;

    private static final Logger logger = Logger.getLogger(PasswordManager.class.getName());

    private static String getPasswordSalt() {
        // TODO: Using fixed salt for now. Change later to use the combination of fixed and variable salt
        return PASSWORD_FIXED_SALT;
    }

    private static String generateOneWayDigest(String password, int iterations) throws NoSuchAlgorithmException {
        if(Strings.isNullOrEmpty(password)) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        if (iterations <= 0 || iterations >= 1000) {
            throw new IllegalArgumentException("Iterations needs to be greater than 0 and less than or equal to 1000");
        }

        String digest = password ;
        for (int i = 0; i < iterations; i++) {
            digest = generateOneWayDigest(digest);
        }

        return digest;
    }

    private static String generateOneWayDigest(String password) throws NoSuchAlgorithmException {
        String digestInput = password;
        MessageDigest messageDigest = MessageDigest.getInstance(HASH_ALGORITHM);
        messageDigest.reset();
        messageDigest.update(digestInput.getBytes());

        return Hex.encodeHexString(messageDigest.digest());
    }

    public static String getEncryptedPassword(String password) throws PasswordManagerException {
        try {
            return generateOneWayDigest(password + getPasswordSalt(), HASH_NUM_ITERATIONS);
        } catch (NoSuchAlgorithmException e) {
            logger.info("ERROR: Could not get an instance of MessageDigest");
            throw new PasswordManagerException("Unable to encrypt password.");
        }
    }

    public static boolean checkPasswordEquality(String password, String encryptedPassword) throws NoSuchAlgorithmException {
        String passwordDigest = generateOneWayDigest(password + getPasswordSalt(), HASH_NUM_ITERATIONS);
        return passwordDigest.equals(encryptedPassword);
    }

}
