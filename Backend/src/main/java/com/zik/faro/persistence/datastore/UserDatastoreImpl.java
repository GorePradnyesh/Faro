package com.zik.faro.persistence.datastore;

import com.googlecode.objectify.Work;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.DatastoreException;
import com.zik.faro.commons.exceptions.UpdateVersionException;
import com.zik.faro.data.user.FaroUser;
import com.zik.faro.persistence.datastore.data.EventDo;
import com.zik.faro.persistence.datastore.data.user.FaroUserDo;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDatastoreImpl {
	private static Logger logger = LoggerFactory.getLogger(UserDatastoreImpl.class);
    
    private static final String FIRST_NAME_FIELD_NAME = "firstName";

    public static void storeUser(final FaroUserDo user){
    	//TODO: Create topic
        DatastoreObjectifyDAL.storeObject(user);
        logger.info("FaroUser created");
    }

    public static FaroUserDo loadFaroUserById(final String userId) throws DataNotFoundException{
        FaroUserDo user = DatastoreObjectifyDAL.loadObjectById(userId, FaroUserDo.class);
        logger.info("FaroUser loaded");
        return user;
    }
    
    public static Map<String, FaroUserDo> loadFaroUsers(List<String> objectIds){
        Map<String, FaroUserDo> users = DatastoreObjectifyDAL.loadMultipleObjectsByIdSync(objectIds, FaroUserDo.class);
        logger.info("Total faroUsers fetched:"+(users == null ? 0 : users.size()));
        return users;
    }

    /* Currently searches only by FirstName, for an exact match */
    public static List<FaroUserDo> loadFaroUsersByName(final String firstName){
        List<FaroUserDo> faroUsers = DatastoreObjectifyDAL.loadObjectsByIndexedStringFieldEQ(FIRST_NAME_FIELD_NAME, firstName, FaroUserDo.class);
        logger.info("Total faroUsers fetched given firstName:"+(faroUsers == null ? 0 : faroUsers.size()));
        return faroUsers;
    }
    
    public static boolean isExistingUser(final String userId){
    	try{
        	if(loadFaroUserById(userId) != null) {
                return true;
            }
        }catch(DataNotFoundException e){
        	return false;
        }
    	return false;
    }
    
    public static FaroUserDo updateFaroUser(final String userId, final FaroUserDo updateObj) throws DataNotFoundException, DatastoreException, UpdateVersionException{
        Work w = new Work<TransactionResult<FaroUserDo>>() {
			
			@Override
			public TransactionResult<FaroUserDo> run() {
				FaroUserDo userDo;
				try {
					userDo = DatastoreObjectifyDAL.loadObjectById(userId, FaroUserDo.class);
					logger.info("FaroUser loaded");
				} catch (DataNotFoundException e) {
					return new TransactionResult<FaroUserDo>(null, TransactionStatus.DATANOTFOUND);
				}
				if(!BaseDatastoreImpl.isVersionOk(updateObj, userDo)){
					return new TransactionResult<FaroUserDo>(null, TransactionStatus.VERSIONMISSMATCH, "Incorrect entity version. Current version:"+userDo.getVersion().toString());
				}
				
				BaseDatastoreImpl.versionIncrement(userDo, updateObj);
                DatastoreObjectifyDAL.storeObject(updateObj);
                return new TransactionResult<FaroUserDo>(updateObj, TransactionStatus.SUCCESS);
			}
		};
        TransactionResult<FaroUserDo> result = DatastoreObjectifyDAL.update(w);
        DatastoreUtil.processResult(result);
        logger.info("FaroUser updated");
        return result.getEntity();
    }
}
