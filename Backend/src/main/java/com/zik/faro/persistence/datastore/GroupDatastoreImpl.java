package com.zik.faro.persistence.datastore;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.objectify.Work;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.DatastoreException;
import com.zik.faro.commons.exceptions.UpdateException;
import com.zik.faro.commons.exceptions.UpdateVersionException;
import com.zik.faro.persistence.datastore.data.GroupDo;
import com.zik.faro.persistence.datastore.data.GroupUserDo;

public class GroupDatastoreImpl {
	private static Logger logger = LoggerFactory.getLogger(GroupDatastoreImpl.class);
    
	public static void storeGroup(final GroupDo group){
    	DatastoreObjectifyDAL.storeObject(group);
    	logger.info("Group stored");
    	// Creating group user relation if group creation succeeds. 
    	for(String faroUserId : group.getParticipants()){
    		GroupUserDatastoreImpl.storeGroupUser(group.getId(), faroUserId);
    		logger.info("GroupUser stored for participant:"+faroUserId);
    	}
	}

	public static GroupDo loadGroupById(final String groupId) throws DataNotFoundException{
		GroupDo groupDo = DatastoreObjectifyDAL.loadObjectById(groupId, GroupDo.class);
		logger.info("Group loaded");
		return groupDo;
	}
	
	public static void deleteGroup(final String groupId) {
        DatastoreObjectifyDAL.delelteObjectById(groupId, GroupDo.class);
        GroupUserDatastoreImpl.deleteGroupUserByGroupId(groupId);
        logger.info("Group deleted");
    }
	
	public static GroupDo updateGroup(final String groupId, final GroupDo updateObj, final Set<String> updatedFields) throws DataNotFoundException, DatastoreException, UpdateVersionException, UpdateException{
        Work w = new Work<TransactionResult<GroupDo>>() {
			
			@Override
			public TransactionResult<GroupDo> run() {
				GroupDo group;
				try {
					group = DatastoreObjectifyDAL.loadObjectById(groupId, GroupDo.class);
					logger.info("Group loaded");
				} catch (DataNotFoundException e) {
					return new TransactionResult<GroupDo>(null, TransactionStatus.DATANOTFOUND);
				}
				if(!BaseDatastoreImpl.isVersionOk(updateObj, group)){
					return new TransactionResult<GroupDo>(null, TransactionStatus.VERSIONMISSMATCH, "Incorrect entity version. Current version:"+group.getVersion().toString());
				}
				
				try {
					BaseDatastoreImpl.updateModifiedFields(group, updateObj, updatedFields);
				} catch (Exception e) {
					return new TransactionResult<GroupDo>(null, TransactionStatus.UPDATEEXCEPTION, "Cannot apply update delta");
				} 
				
				BaseDatastoreImpl.versionIncrement(updateObj, group);
                DatastoreObjectifyDAL.storeObject(group);
                return new TransactionResult<GroupDo>(group, TransactionStatus.SUCCESS);
			}
		};
        TransactionResult<GroupDo> result = DatastoreObjectifyDAL.update(w);
        DatastoreUtil.processResult(result);
        logger.info("Group updated");
        return result.getEntity();
    }
	
	public static GroupDo updateParticipants(final List<String> toBeAdded, final List<String> toBeRemoved, 
    		final String groupId, final Long version) throws DatastoreException, DataNotFoundException, UpdateVersionException, UpdateException{
    	Work w = new Work<TransactionResult<GroupDo>>() {
	        public TransactionResult<GroupDo> run() {
	        	// Read from datastore
	        	GroupDo group = null;
				try {
					group = loadGroupById(groupId);
					logger.info("Group loaded!");
				} catch (DataNotFoundException e) {
					return new TransactionResult<GroupDo>(null, TransactionStatus.DATANOTFOUND);
				}
				
				if(!BaseDatastoreImpl.isVersionOk(version, group.getVersion())){
					return new TransactionResult<GroupDo>(null, TransactionStatus.VERSIONMISSMATCH, "Incorrect entity version. Current version:"+group.getVersion().toString());
				}
				
				if(toBeRemoved != null)
					group.getParticipants().removeAll(toBeRemoved);
				if(toBeAdded != null)
					group.getParticipants().addAll(toBeAdded);
				
				BaseDatastoreImpl.versionIncrement(group);

	            // Store
	            DatastoreObjectifyDAL.storeObject(group);
	            
	            return new TransactionResult<GroupDo>(group, TransactionStatus.SUCCESS);
	        }
	    };
	    
    	TransactionResult<GroupDo> result = DatastoreObjectifyDAL.update(w);
    	DatastoreUtil.processResult(result);
		logger.info("Group updated!");
		// Remove GroupUser relation for toBeRemoved participants
		if(toBeRemoved != null){
			for(String faroUserId: toBeRemoved){
				GroupUserDatastoreImpl.deleteGroupUser(groupId, faroUserId);
				logger.info("Group User Deleted for faroUserId:"+faroUserId);
			}
		}
		
		// Add GroupUser relation for toBeAdded participants
		if(toBeAdded != null){
			for(String faroUserId: toBeAdded){
				GroupUserDo groupUserDo = GroupUserDatastoreImpl.loadGroupUser(groupId, faroUserId);
				if(groupUserDo == null){
					 GroupUserDatastoreImpl.storeGroupUser(groupId, faroUserId);
					 logger.info("Group User Created for faroUserId:"+faroUserId);
				}
			}
		}
		
    	return result.getEntity();
    }
	
	public static GroupDo updateAdmins(final List<String> toBeAdded, final List<String> toBeRemoved, 
    		final String groupId, final Long version) throws DatastoreException, DataNotFoundException, UpdateVersionException, UpdateException{
    	Work w = new Work<TransactionResult<GroupDo>>() {
	        public TransactionResult<GroupDo> run() {
	        	// Read from datastore
	        	GroupDo group = null;
				try {
					group = loadGroupById(groupId);
					logger.info("Group loaded!");
				} catch (DataNotFoundException e) {
					return new TransactionResult<GroupDo>(null, TransactionStatus.DATANOTFOUND);
				}
				
				if(!BaseDatastoreImpl.isVersionOk(version, group.getVersion())){
					return new TransactionResult<GroupDo>(null, TransactionStatus.VERSIONMISSMATCH, "Incorrect entity version. Current version:"+group.getVersion().toString());
				}
				
				if(toBeRemoved != null)
					group.getAdmins().removeAll(toBeRemoved);
				if(toBeAdded != null)
					group.getAdmins().addAll(toBeAdded);
				
				BaseDatastoreImpl.versionIncrement(group);

	            // Store
				DatastoreObjectifyDAL.storeObject(group);
	            
	            return new TransactionResult<GroupDo>(group, TransactionStatus.SUCCESS);
	        }
	    };
	    
    	TransactionResult<GroupDo> result = DatastoreObjectifyDAL.update(w);
    	DatastoreUtil.processResult(result);
		logger.info("Group updated!");
		return result.getEntity();
    }
	
    
}
