package com.zik.faro.applogic;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.DatastoreException;
import com.zik.faro.commons.exceptions.FaroUnauthorizedException;
import com.zik.faro.commons.exceptions.UpdateException;
import com.zik.faro.commons.exceptions.UpdateVersionException;
import com.zik.faro.data.Group;
import com.zik.faro.persistence.datastore.GroupDatastoreImpl;
import com.zik.faro.persistence.datastore.data.GroupDo;

public class GroupManagement {
	private  static final Logger logger = LoggerFactory.getLogger(GroupManagement.class);
	
	public static Group createGroup(Group group){
		GroupDo groupDo = ConversionUtils.toDo(group);
		groupDo.createIdAndInitialize();
		GroupDatastoreImpl.storeGroup(groupDo);
		return ConversionUtils.fromDo(groupDo);
	}
	
	public static Group getGroupDetails(String groupId, String userId) throws DataNotFoundException, FaroUnauthorizedException{
		GroupDo groupDo = GroupDatastoreImpl.loadGroupById(groupId);
		if(groupDo.getParticipants().contains(userId)){
			return ConversionUtils.fromDo(groupDo);
		}else{
			throw new FaroUnauthorizedException("UserId:"+userId+" does not belong to the group");
		}
	}
	
	public static Group updateGroup(Group group, Set<String> updatedFields, String faroUserId) throws DatastoreException, DataNotFoundException, UpdateVersionException, UpdateException, FaroUnauthorizedException{
		if(isAuthorized(faroUserId, group.getId())){
			GroupDo groupDo = GroupDatastoreImpl.updateGroup(group.getId(), 
					ConversionUtils.toDo(group), updatedFields);
			return ConversionUtils.fromDo(groupDo);
		}
		throw new UpdateException("Internal Server Error");
	}
	
	public static Group updateGroupParticipants(final String groupId, final String faroUserId,
			final List<String> toBeAdded, final List<String> toBeRemoved, Long version) throws DatastoreException, DataNotFoundException, UpdateVersionException, UpdateException, FaroUnauthorizedException {
		if(isAuthorized(faroUserId, groupId)){
			GroupDo groupDo = GroupDatastoreImpl.updateParticipants(toBeAdded, 
					toBeRemoved, groupId, version);
			return ConversionUtils.fromDo(groupDo);
		}
		throw new UpdateException("Internal Server Error");
	}
	
	public static Group updateGroupAdmins(final String groupId, final String faroUserId,
			final List<String> toBeAdded, final List<String> toBeRemoved, Long version) throws DatastoreException, DataNotFoundException, UpdateVersionException, UpdateException, FaroUnauthorizedException {
		if(isAuthorized(faroUserId, groupId)){
			GroupDo groupDo = GroupDatastoreImpl.updateAdmins(toBeAdded, 
					toBeRemoved, groupId, version);
			return ConversionUtils.fromDo(groupDo);
		}
		throw new UpdateException("Internal Server Error");
	}
	
	private static boolean isAuthorized(String faroUserId, String groupId) throws DataNotFoundException, FaroUnauthorizedException{
		GroupDo groupDo = GroupDatastoreImpl.loadGroupById(groupId);
		if(groupDo.getAdmins().contains(faroUserId)){
			return true;
		}else{
			throw new FaroUnauthorizedException("User:"+faroUserId+" is not one of the admins");
		}
	}
}
