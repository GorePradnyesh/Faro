package com.zik.faro.api.base;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseCredentials;
import com.zik.faro.persistence.datastore.StaticInitializer;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Refer this document for more info
 * https://cloud.google.com/appengine/docs/java/config/appconfig?csw=1#Using_a_ServletContextListener
 * OR
 * https://cloud.google.com/appengine/docs/java/config/appconfig
 */
public class DefaultServletContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        System.out.println("---- INITIALIZING THE CUSTOM SERVLET CONTEXT @pgore ----");
        //This will register all the necessary classes for using Objectify.
        StaticInitializer.init();

        // Initialize the Firebase Admin SDK
        FileInputStream serviceAccount = null;
        try {
            // use the path to serviceAccountKey.json
            serviceAccount = new FileInputStream(new File("WEB-INF/classes/faro-56043-firebase-adminsdk-out3y-192c0b32ad.json"));
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredential(FirebaseCredentials.fromCertificate(serviceAccount))
                    .setDatabaseUrl("https://faro-56043.firebaseio.com/")
                    .build();

            FirebaseApp.initializeApp(options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        //Ignored. App engine will not invoke this method anyway.
    }
}
