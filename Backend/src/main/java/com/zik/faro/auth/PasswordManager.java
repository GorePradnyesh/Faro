package com.zik.faro.auth;

import com.auth0.jwt.internal.org.apache.commons.codec.binary.Hex;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by granganathan on 2/15/15.
 */

/**
 * Class for encrypting password and to compare
 * a given password with an encrypted password from the datastore
 */
public class PasswordManager {
    // TODO: Use a better salt string
    private static final String PASSWORD_FIXED_SALT = "iamthenight$";
    private static final String HASH_ALGORITHM = "SHA";
    // TODO: determine optimal number of iterations
    private static final int HASH_NUM_ITERATIONS = 1;

    private static final Logger logger = LoggerFactory.getLogger(PasswordManager.class);

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

    /**
     * The password is suffixed with a salt and a digest is created
     * by running through the same digest creation process for a
     * defined number of iterations
     *
     * @param password
     * @return encrypted password string
     * @throws PasswordManagerException
     */
    public static String getEncryptedPassword(String password) throws PasswordManagerException {
        try {
            return generateOneWayDigest(password + getPasswordSalt(), HASH_NUM_ITERATIONS);
        } catch (NoSuchAlgorithmException e) {
            logger.error("Could not get an instance of MessageDigest");
            throw new PasswordManagerException("Unable to encrypt password.");
        }
    }

    /**
     *
     * @param password
     * @param encryptedPassword
     * @return true if encryptedPassword is the same as the string obtained on encrypting password;
     *         otherwise false
     * @throws PasswordManagerException
     */
    public static boolean checkPasswordEquality(String password, String encryptedPassword) throws PasswordManagerException {
        String passwordDigest = null;
        try {
            passwordDigest = generateOneWayDigest(password + getPasswordSalt(), HASH_NUM_ITERATIONS);
        } catch (NoSuchAlgorithmException e) {
            logger.error("Could not get an instance of MessageDigest");
            throw new PasswordManagerException("Unable to check the equality of the passwords.");
        }
        return passwordDigest.equals(encryptedPassword);
    }

}
