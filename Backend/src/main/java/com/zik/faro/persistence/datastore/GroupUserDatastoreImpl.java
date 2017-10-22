package com.zik.faro.persistence.datastore;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.cmd.Query;
import com.zik.faro.persistence.datastore.data.GroupDo;
import com.zik.faro.persistence.datastore.data.GroupUserDo;
import com.zik.faro.persistence.datastore.data.user.FaroUserDo;

public class GroupUserDatastoreImpl {
	private static Logger logger = LoggerFactory.getLogger(GroupUserDatastoreImpl.class);
    
    public static final String GROUP_REF_FIELD_NAME = "groupRef";
    public static final String FARO_USER_REF_FIELD_NAME = "faroUserRef";

    public static void storeGroupUser(final String groupId, final String faroUserId){
        GroupUserDo groupUserDo = new GroupUserDo(groupId, faroUserId);
        DatastoreObjectifyDAL.storeObject(groupUserDo);
        logger.info("GroupUser stored");
    }
  
	public static GroupUserDo loadGroupUser(final String groupId, final String faroUserId){
        Ref<GroupDo> groupRef = DatastoreObjectifyDAL.getRefForClassById(groupId, GroupDo.class);
        Ref<FaroUserDo> faroUserRef = DatastoreObjectifyDAL.getRefForClassById(faroUserId, FaroUserDo.class);
        
        Query<GroupUserDo> groupUserQuery = ofy().load().type(GroupUserDo.class);
        groupUserQuery = groupUserQuery.filter(GROUP_REF_FIELD_NAME, groupRef);
        groupUserQuery = groupUserQuery.filter(FARO_USER_REF_FIELD_NAME, faroUserRef);
        // Return the first result since there should be another record with same key.
        GroupUserDo groupUserDo = groupUserQuery.first().now();
        logger.info("GroupUser loaded");
        return groupUserDo;
     }
    
    public static List<GroupUserDo> loadGroupUserByGroupId(final String groupId){
        List<GroupUserDo> groupUserList =
                DatastoreObjectifyDAL.loadObjectsByIndexedRefFieldEQ(GROUP_REF_FIELD_NAME,
                        GroupDo.class,
                        groupId,
                        GroupUserDo.class);
        logger.info("Total groupUsers fetched given groupId:"+(groupUserList == null ? 0 : groupUserList.size()));
        return groupUserList;
    }
    
    public static List<GroupUserDo> loadGroupUserByFaroUserId(final String faroUserId){
        List<GroupUserDo> groupUserList =
                DatastoreObjectifyDAL.loadObjectsByIndexedRefFieldEQ(FARO_USER_REF_FIELD_NAME,
                        FaroUserDo.class,
                        faroUserId,
                        GroupUserDo.class);
        logger.info("Total groupUsers fetched given faroId:"+(groupUserList == null ? 0 : groupUserList.size()));
        return groupUserList;
    }

    public static void deleteGroupUserByGroupId(final String groupId){
        DatastoreObjectifyDAL.deleteObjectsByIndexedRefFieldEQ(GROUP_REF_FIELD_NAME,
                GroupDo.class,
                groupId,
                GroupUserDo.class);
        logger.info("GroupUser deleted given groupId");
    }
    
    public static void deleteGroupUserByFaroUserId(final String faroUserId){
        DatastoreObjectifyDAL.deleteObjectsByIndexedRefFieldEQ(FARO_USER_REF_FIELD_NAME,
                FaroUserDo.class,
                faroUserId,
                GroupUserDo.class);
        logger.info("GroupUser deleted given faroUserId");
    }
    
    public static void deleteGroupUser(final String groupId, final String faroUserId){
    	GroupUserDo groupUser = new GroupUserDo(groupId, faroUserId);
    	DatastoreObjectifyDAL.delelteObjectById(groupUser.getId(), GroupUserDo.class);
    	logger.info("GroupUser deleted");
    }
    
}
