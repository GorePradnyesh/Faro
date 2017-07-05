package com.zik.faro.api.base;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseCredentials;
import com.zik.faro.commons.ConfigPropertiesUtil;
import com.zik.faro.persistence.datastore.StaticInitializer;
import org.apache.log4j.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.MessageFormat;

/**
 * Refer this document for more info
 * https://cloud.google.com/appengine/docs/java/config/appconfig?csw=1#Using_a_ServletContextListener
 * OR
 * https://cloud.google.com/appengine/docs/java/config/appconfig
 */
public class DefaultServletContextListener implements ServletContextListener {
    private static final Logger logger = Logger.getLogger(DefaultServletContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        logger.info("---- INITIALIZING THE CUSTOM SERVLET CONTEXT @pgore ----");
        // This will register all the necessary classes for using Objectify and load server config props
        try {
            StaticInitializer.init();

            // Initialize the Firebase Admin SDK
            // use the path to serviceAccountKey json
            FileInputStream serviceAccount = new FileInputStream(new File(MessageFormat.format("WEB-INF/classes/{0}",
                    ConfigPropertiesUtil.getFirebaseServiceAccountJsonFileName())));
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredential(FirebaseCredentials.fromCertificate(serviceAccount))
                    .setDatabaseUrl(ConfigPropertiesUtil.getFirebaseDatabaseUrl())
                    .build();

            FirebaseApp.initializeApp(options);
        } catch (IOException e) {
            logger.error("Failed to configure app server", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        //Ignored. App engine will not invoke this method anyway.
    }
}
