package com.zik.faro.api.base;

import com.zik.faro.persistence.datastore.StaticInitializer;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

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
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        //Ignored. App engine will not invoke this method anyway.
    }
}
