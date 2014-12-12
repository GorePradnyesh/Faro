package com.zik.faro.persistence.datastore;

import com.google.appengine.api.datastore.Entity;
import com.googlecode.objectify.Key;

import java.lang.annotation.Annotation;

import static com.googlecode.objectify.ObjectifyService.ofy;

public class DatastoreObjectifyDAL {
    private static boolean enableReflectionCheck = true;

    public static <T> Key<T> storeObject(final T object){
        if(enableReflectionCheck){
            //TODO: ensure that the object class has been annotated with @Entity
        }
        Key<T> key = ofy().save().entity(object).now();
        //logger.info("Successfully stored : {}", key.getId())
        return key;
    }

    public static <T> T loadObject(final String objectId, Class<T> clazz){
        Key<T> objectKey = Key.create(clazz, objectId);
        T object = ofy().load().key(objectKey).now();
        return object;
    }
}
