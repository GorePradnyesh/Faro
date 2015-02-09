package com.zik.faro.persistence.datastore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjectifyHelper {

    private static final String EVENT_ID_DEFAULT_FIELD_NAME = "eventId";

    /**
     * This method loads the object which has its own ID and an Indexed eventId field. The combination of
     * the objectId and eventId should be universally unique.
     * @param objectId
     * @param eventIdFieldName
     * @param eventId
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T loadObjectByIdAndEventIdField(final String objectId,
                                                      final String eventIdFieldName,
                                                      final String eventId,
                                                      final Class<T> clazz){
        Map<DatastoreOperator, String> filterKeyMap = new HashMap<>();
        filterKeyMap.put(DatastoreOperator.EQ, objectId);

        Map<String, String> filterMap = new HashMap<>();
        filterMap.put(eventIdFieldName, eventId);

        List<T> activityList =
                DatastoreObjectifyDAL.loadObjectsByStringFilters(filterKeyMap, filterMap, clazz);
        if(activityList.size() > 1){
            throw new RuntimeException("Unexpected condition !! Got multiple "+ clazz.getCanonicalName() +" objects for single Id");
        }
        if(activityList.size() == 0 ){
            return null; //TODO: better response ?
        }
        return activityList.get(0);
    }

    public static <T> T loadObjectByIdAndEventIdField(final String objectId,
                                                      final String eventId,
                                                      final Class<T> clazz){
        return loadObjectByIdAndEventIdField(objectId, EVENT_ID_DEFAULT_FIELD_NAME, eventId, clazz);
    }


    public static <T> List<T> loadObjectsForEventId(final String eventIdFieldName,
                                                    final String eventId,
                                                    final Class<T> clazz){
        Map<DatastoreOperator, String> filterKeyMap = new HashMap<>();          //Empty filter map

        Map<String, String> filterMap = new HashMap<>();                        //Valid filter map
        filterMap.put(eventIdFieldName, eventId);

        List<T> objectList =
                DatastoreObjectifyDAL.loadObjectsByStringFilters(filterKeyMap, filterMap, clazz);
        return objectList;
    }

}
