package com.zik.faro.persistence.datastore;


import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.objectify.Work;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.DatastoreException;
import com.zik.faro.commons.exceptions.UpdateException;
import com.zik.faro.commons.exceptions.UpdateVersionException;
import com.zik.faro.data.user.EventInviteStatus;
import com.zik.faro.persistence.datastore.data.EventDo;

public class EventDatastoreImpl {       
	private static Logger logger = LoggerFactory.getLogger(EventDatastoreImpl.class);
    
	public static void storeEvent(final String userId, final EventDo event){
    	DatastoreObjectifyDAL.storeObject(event);
    	logger.info("Event stored");
    	// Creating event user relation if event creation succeeds. Owner will be userId too and since owner created event, he has by default accepted it
    	EventUserDatastoreImpl.storeEventUser(event.getId(), userId, userId, EventInviteStatus.ACCEPTED);
    }
    
    // For use cases where we do not need to store eventuser relation
    public static void storeEventOnly(final EventDo event){
    	DatastoreObjectifyDAL.storeObject(event);
    	logger.info("Event stored");
    }

    public static EventDo loadEventByID(final String eventId) throws DataNotFoundException{
        EventDo event = DatastoreObjectifyDAL.loadObjectById(eventId, EventDo.class);
        logger.info("Event loaded");
        return event;
    }
    
    public static EventDo updateEvent(final String eventId, final EventDo updateObj, final Set<String> updatedFields) throws DataNotFoundException, DatastoreException, UpdateVersionException, UpdateException{
        Work w = new Work<TransactionResult<EventDo>>() {
			
			@Override
			public TransactionResult<EventDo> run() {
				EventDo event;
				try {
					event = DatastoreObjectifyDAL.loadObjectById(eventId, EventDo.class);
					logger.info("Event loaded");
				} catch (DataNotFoundException e) {
					return new TransactionResult<EventDo>(null, TransactionStatus.DATANOTFOUND);
				}
				if(!BaseDatastoreImpl.isVersionOk(updateObj, event)){
					return new TransactionResult<EventDo>(null, TransactionStatus.VERSIONMISSMATCH, "Incorrect entity version. Current version:"+event.getVersion().toString());
				}
				
				try {
					BaseDatastoreImpl.updateModifiedFields(event, updateObj, updatedFields);
				} catch (Exception e) {
					return new TransactionResult<EventDo>(null, TransactionStatus.UPDATEEXCEPTION, "Cannot apply update delta");
				} 
				
				BaseDatastoreImpl.versionIncrement(updateObj, event);
                DatastoreObjectifyDAL.storeObject(event);
                return new TransactionResult<EventDo>(event, TransactionStatus.SUCCESS);
			}
		};
        TransactionResult<EventDo> result = DatastoreObjectifyDAL.update(w);
        DatastoreUtil.processResult(result);
        logger.info("Event updated");
        return result.getEntity();
    }
    
    
    
    public static void deleteEvent(final String eventId) {
        DatastoreObjectifyDAL.delelteObjectById(eventId, EventDo.class);
        logger.info("Event deleted");
    }
}
