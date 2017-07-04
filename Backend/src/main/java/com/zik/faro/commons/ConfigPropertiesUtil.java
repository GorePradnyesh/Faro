package com.zik.faro.commons;

import com.zik.faro.api.base.DefaultServletContextListener;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Properties;

/**
 * Created by gaurav on 7/4/17.
 */
public class ConfigPropertiesUtil {
    private static final Logger logger = Logger.getLogger(ConfigPropertiesUtil.class);
    private static final String PROPS_FILE_NAME = "config.properties";
    private static final Properties properties = new Properties();

    private static final String FIREBASE_AUTHORIZATION_KEY = "firebaseAuthorizationKey";
    private static final String FIREBASE_SERVICE_ACCOUNT_JSON_FILE_NAME = "firebaseServiceAccountJsonFile";
    private static final String FIREBASE_DATABASE_URL = "firebaseDatabaseUrl";

    private static String firebaseAuthorizationKey;
    private static String firebaseServiceAccountJsonFileName;
    private static String firebaseDatabaseUrl;

    public static void loadPropertiesFile() throws IOException {
        properties.load(new FileInputStream(new File(MessageFormat.format("WEB-INF/classes/{0}",
                PROPS_FILE_NAME))));

        firebaseAuthorizationKey = properties.getProperty(FIREBASE_AUTHORIZATION_KEY);
        firebaseServiceAccountJsonFileName = properties.getProperty(FIREBASE_SERVICE_ACCOUNT_JSON_FILE_NAME);
        firebaseDatabaseUrl = properties.getProperty(FIREBASE_DATABASE_URL);

        logger.info(MessageFormat.format("Successfully loaded props from file. " +
                "firebaseAuthorizationKey = {0}, firebaseServiceAccountJsonFileName = {1}",
                getFirebaseAuthorizationKey(), getFirebaseServiceAccountJsonFileName()));
    }

    public static String getFirebaseAuthorizationKey() {
        return firebaseAuthorizationKey;
    }

    public static String getFirebaseServiceAccountJsonFileName() {
        return firebaseServiceAccountJsonFileName;
    }

    public static String getFirebaseDatabaseUrl() {
        return firebaseDatabaseUrl;
    }
}
