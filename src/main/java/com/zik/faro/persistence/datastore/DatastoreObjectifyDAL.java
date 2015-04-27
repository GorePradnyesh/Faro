package com.zik.faro.persistence.datastore;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.Work;
import com.googlecode.objectify.cmd.Query;
import com.zik.faro.commons.Constants;
import com.zik.faro.commons.exceptions.DataNotFoundException;

/**
 * This the lowermost layer which abstracts the fundamental load store operations.
 */
public class DatastoreObjectifyDAL {
    private static boolean enableReflectionCheck = true;

    //===================== HELPER FUNCTIONS FOR FILTERING INDEXED STRING FIELDS =====================

    public static <T> Key<T> storeObject(final T object){
       
        Key<T> key = ofy().save().entity(object).now();
        //logger.info("Successfully stored : {}", key.getId())
        return key;
    }

    public static <T> T loadObjectById(final String objectId, Class<T> clazz) throws DataNotFoundException{
        Key<T> objectKey = Key.create(clazz, objectId);
        T object = ofy().load().key(objectKey).now();
        if(object == null){
        	throw new DataNotFoundException("Data not found. Key:" + objectKey.toString());
        }
        return object;
    }
    
    public static <T,V> void deleteObjectByIdWithParentId(final String objectId,
    		Class<T> clazz, final String parentId, Class<V> parentClazz){
    	Key<V> parentKey = Key.create(parentClazz, parentId);
    	ofy().delete().type(clazz).parent(parentKey).id(objectId).now();
    }
    
    public static <T,V> void deleteObjectsByIdWithParentId(final List<String> objectIds,
    		Class<T> clazz, final String parentId, Class<V> parentClazz){
    	Key<V> parentKey = Key.create(parentClazz, parentId);
    	// Note: This is a bulk call. Deletes will be async.
    	ofy().delete().type(clazz).parent(parentKey).ids(objectIds);
    }
    
    public static <T> void delelteObjectById(final String objectId, Class<T> clazz){
    	ofy().delete().type(clazz).id(objectId).now();
    }
    
    public static <T> void delelteObjectsById(final List<String> objectIds, Class<T> clazz){
    	// Note: This is a bulk call. Deletes will be async.
    	ofy().delete().type(clazz).ids(objectIds);
    }
    
    public static <T> void deleteEntity(final T entity){
    	ofy().delete().entity(entity).now();
    }
    
    public static <T> void deleteEntities(final List<T> entities){
    	// Note: This is a bulk call. Deletes will be async.
    	ofy().delete().entities(entities);
    }
    
    public static <T> Map<String, T> loadMultipleObjectsByIdSync(List<String> objectIds, Class<T> clazz){
        List<Key<T>> keys = new ArrayList<>();
        Map<String, T> objectMap = new HashMap<>();
        for(String objectId: objectIds){
            keys.add(Key.create(clazz, objectId));
        }
        Map<Key<T>, T> keyObjectMap = ofy().load().keys(keys);
        if(keyObjectMap !=null && !keyObjectMap.isEmpty()){
            for (Map.Entry<Key<T>, T> entry : keyObjectMap.entrySet()) {
                Key<T> key = entry.getKey();
                T value = entry.getValue();
                objectMap.put(key.getName(), value);
            }
        }
        return objectMap;
    }

    public static <T,V> T loadObjectWithParentId(final Class<V> parentClazz,
                                                 final String parentIdValue,
                                                 final Class<T> clazz,
                                                 final String objectId) throws DataNotFoundException{
        Key<T> objectKey = Key.create(Key.create(parentClazz, parentIdValue), clazz, objectId);
        T object = ofy().load().key(objectKey).now();
        if(object == null){
        	throw new DataNotFoundException("Data not found. Key:" + objectKey.toString());
        }
        return object;
    }
    
    public static <T,V> List<T> loadObjectsByAncestorRef(final Class<V> parentClazz,
                                                      final String parentIdValue,
                                                      final Class<T> clazz){
        Ref<V> parentKey = Ref.create(Key.create(parentClazz, parentIdValue));
        List<T> objectList = ofy().load().type(clazz).ancestor(parentKey).
        		limit(Constants.MAX_ITEMS_TO_FETCH_FROM_DATASTORE).list();
        return objectList;
    }


    public static <T> List<T> loadObjectsByIndexedStringFieldEQ(final String fieldName, final String fieldValue, Class<T> clazz){
        //TODO: do reflection validation to ensure that fieldName provided is Annotated with @Index
        List<T> objectList = ofy().load().type(clazz).filter(fieldName, fieldValue)
        		.limit(Constants.MAX_ITEMS_TO_FETCH_FROM_DATASTORE).list();
        return objectList;
    }

    public static <T> T loadFirstObjectByIndexedStringFieldEQ(final String fieldName, 
    		final String fieldValue, Class<T> clazz) throws DataNotFoundException {
        T object = ofy().load().type(clazz).filter(fieldName, fieldValue).first().now();
        if(object == null){
        	throw new DataNotFoundException("Data not found. IndexedFieldName:" + fieldName + " IndexedFieldValue:"+fieldValue);
        }
        return object;
    }


    //TODO: Use a cleaner way to use Operators with the filters provided ( DatastoreOperator Enum etc ) currently the operators are expected in the incoming string
    public static <T> List<T> loadObjectsByStringFilters(Map<DatastoreOperator, String> keyFilterMap,
                                                         Map<String, String> filterMap,
                                                         Class<T> clazz){
        Query<T> query = ofy().load().type(clazz);
        query = createAndAppendKeyFilters(keyFilterMap, query, clazz);
        query = createAndAppendFilters(filterMap, query);

        List<T> resultSet = query.limit(Constants.MAX_ITEMS_TO_FETCH_FROM_DATASTORE).list();
        return resultSet;
    }


    //===================== HELPER FUNCTIONS FOR FILTERING BY INDEXED REFERENCE FIELDS =====================


    public static <T> Ref<T> getRefForClassById(final String id, final Class<T> clazz){
        return Ref.create(Key.create(clazz, id));
    }

    public static <T> T loadObjectByIndexedRefFieldEQ(final String filterFieldName,
                                                      final Class filterFieldClass,
                                                      final String filterFieldValue,
                                                      Class<T> clazz) throws DataNotFoundException{
        Ref<T> filterRef = getRefForClassById(filterFieldValue, filterFieldClass);
        T object = ofy().load().type(clazz).filter(filterFieldName, filterRef).first().now();
        if(object == null){
        	throw new DataNotFoundException("Data not found. Key:" + filterRef.getKey().toString());
        }
        return object;
    }
    
   
    public static <T> List<T> loadObjectsByIndexedRefFieldEQ(final String filterFieldName,
                                                      final Class filterFieldClass,
                                                      final String filterFieldValue,
                                                      Class<T> clazz){
        Ref<T> filterRef = getRefForClassById(filterFieldValue, filterFieldClass);
        List<T> objectList = ofy().load().type(clazz).filter(filterFieldName, filterRef).
        		list();
        return objectList;
    }
    
    public static <T> List<T> loadObjectsByIndexedRefFieldEQ(final String filterFieldName,
            final Class filterFieldClass,
            final String filterFieldValue,
            Class<T> clazz,
            final int count){
		Ref<T> filterRef = getRefForClassById(filterFieldValue, filterFieldClass);
		List<T> objectList = ofy().load().type(clazz).filter(filterFieldName, filterRef).
				limit(count).list();
		return objectList;
	} 
    
    //===================== HELPER FUNCTIONS =====================

    public static <T> Query<T> createAndAppendKeyFilters(Map<DatastoreOperator, String> keyFilterMap, Query query, Class<T> clazz){
        for(Map.Entry<DatastoreOperator, String> filterKey: keyFilterMap.entrySet()){
            Key<T> objectKey = Key.create(clazz, filterKey.getValue());
            if(filterKey.getKey() != null) {
                query = query.filterKey(filterKey.getKey().toString(), objectKey);
            }else{
                query = query.filterKey(DatastoreOperator.EQ.toString(), objectKey);
            }
        }
        return query;
    }

    public static <T> Query<T> createAndAppendFilters(Map<String, String> filterMap, Query query){
        for(Map.Entry<String, String> filter: filterMap.entrySet()){
            query = query.filter(filter.getKey(), filter.getValue());
        }
        return query;
    }
    
    public static TransactionResult update(Work w){
    	return ofy().transact(w);
    }

    //TODO: ADD CURSOR COUNTERPARTS / ARGUMENTS FOR THE THE FUNCTIONS ABOVE
}
