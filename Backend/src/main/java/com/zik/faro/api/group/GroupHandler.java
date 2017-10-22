package com.zik.faro.api.group;

import static com.zik.faro.commons.Constants.GROUP_CREATE_PATH_CONST;
import static com.zik.faro.commons.Constants.GROUP_DETAILS_PATH_CONST;
import static com.zik.faro.commons.Constants.GROUP_ID_PATH_PARAM;
import static com.zik.faro.commons.Constants.GROUP_ID_PATH_PARAM_STRING;
import static com.zik.faro.commons.Constants.GROUP_PATH_CONST;
import static com.zik.faro.commons.Constants.GROUP_UPDATE_ADMINS_PATH_CONST;
import static com.zik.faro.commons.Constants.GROUP_UPDATE_PARTICIPANTS_PATH_CONST;
import static com.zik.faro.commons.Constants.GROUP_UPDATE_PATH_CONST;

import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.JResponse;
import com.zik.faro.applogic.GroupManagement;
import com.zik.faro.commons.FaroResponseStatus;
import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.DatastoreException;
import com.zik.faro.commons.exceptions.FaroUnauthorizedException;
import com.zik.faro.commons.exceptions.FaroWebAppException;
import com.zik.faro.commons.exceptions.UpdateException;
import com.zik.faro.commons.exceptions.UpdateVersionException;
import com.zik.faro.data.Group;
import com.zik.faro.data.UpdateCollectionRequest;
import com.zik.faro.data.UpdateRequest;

@Path(GROUP_PATH_CONST)
public class GroupHandler {
	@Context
	SecurityContext context;
	
	private static Logger logger = LoggerFactory.getLogger(GroupHandler.class);
	
	@Path(GROUP_CREATE_PATH_CONST)
    @POST
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public JResponse<Group> createGroup(final Group group){
		String userId = context.getUserPrincipal().getName();
		Group createdGroup = GroupManagement.createGroup(group);
		return JResponse.ok(createdGroup).status(Response.Status.OK).build();
    }
	
	@Path(GROUP_ID_PATH_PARAM_STRING+GROUP_UPDATE_PATH_CONST)
    @PUT
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public JResponse<Group> updateGroup(@PathParam(GROUP_ID_PATH_PARAM) final String groupId, 
    		final UpdateRequest<Group> updateObj){
		String userId = context.getUserPrincipal().getName();
		Set<String> updatedFields = updateObj.getUpdatedFields();
		Group updatedGroup = updateObj.getUpdate();
		Group updateResponse;
		try {
			updateResponse = GroupManagement.updateGroup(updatedGroup, updatedFields, userId);
		}  catch (DataNotFoundException e) {
        	throw new FaroWebAppException(FaroResponseStatus.NOT_FOUND, "Data Not Found");
        } catch (DatastoreException e) {
        	throw new FaroWebAppException(FaroResponseStatus.UNEXPECTED_ERROR, "Datastore Exception");
		} catch (UpdateVersionException e) {
			throw new FaroWebAppException(FaroResponseStatus.UPDATE_VERSION_MISMATCH, "Update Version Mismatch");
		} catch (UpdateException e) {
			throw new FaroWebAppException(FaroResponseStatus.UNEXPECTED_ERROR, "Update Error");
		} catch (FaroUnauthorizedException e) {
			throw new FaroWebAppException(FaroResponseStatus.UNAUTHORIZED, e.getMessage());
		}
		return JResponse.ok(updateResponse).status(Response.Status.OK).build();
	}
	
	@Path(GROUP_ID_PATH_PARAM_STRING+GROUP_UPDATE_PARTICIPANTS_PATH_CONST)
    @PUT
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public JResponse<Group> updateGroupParticipants(@PathParam(GROUP_ID_PATH_PARAM) final String groupId, 
    		final UpdateCollectionRequest<Group,String> updateObj){
		String userId = context.getUserPrincipal().getName();
		Group updatedGroup = updateObj.getUpdate();
		Group updateResponse;
		try {
			updateResponse = GroupManagement.updateGroupParticipants(groupId, userId, 
					updateObj.getToBeAdded(), updateObj.getToBeRemoved(), updatedGroup.getVersion());
		} catch (DataNotFoundException e) {
        	throw new FaroWebAppException(FaroResponseStatus.NOT_FOUND, "Data Not Found");
        } catch (DatastoreException e) {
        	throw new FaroWebAppException(FaroResponseStatus.UNEXPECTED_ERROR, "Datastore Exception");
		} catch (UpdateVersionException e) {
			throw new FaroWebAppException(FaroResponseStatus.UPDATE_VERSION_MISMATCH, "Update Version Mismatch");
		} catch (UpdateException e) {
			throw new FaroWebAppException(FaroResponseStatus.UNEXPECTED_ERROR, "Update Error");
		} catch (FaroUnauthorizedException e) {
			throw new FaroWebAppException(FaroResponseStatus.UNAUTHORIZED, e.getMessage());
		}
		return JResponse.ok(updateResponse).status(Response.Status.OK).build();
	}
	
	@Path(GROUP_ID_PATH_PARAM_STRING+GROUP_UPDATE_ADMINS_PATH_CONST)
    @PUT
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public JResponse<Group> updateGroupAdmins(@PathParam(GROUP_ID_PATH_PARAM) final String groupId, 
    		final UpdateCollectionRequest<Group,String> updateObj){
		String userId = context.getUserPrincipal().getName();
		Group updatedGroup = updateObj.getUpdate();
		Group updateResponse;
		try {
			updateResponse = GroupManagement.updateGroupAdmins(groupId, userId, 
					updateObj.getToBeAdded(), updateObj.getToBeRemoved(), updatedGroup.getVersion());
		} catch (DataNotFoundException e) {
        	throw new FaroWebAppException(FaroResponseStatus.NOT_FOUND, "Data Not Found");
        } catch (DatastoreException e) {
        	throw new FaroWebAppException(FaroResponseStatus.UNEXPECTED_ERROR, "Datastore Exception");
		} catch (UpdateVersionException e) {
			throw new FaroWebAppException(FaroResponseStatus.UPDATE_VERSION_MISMATCH, "Update Version Mismatch");
		} catch (UpdateException e) {
			throw new FaroWebAppException(FaroResponseStatus.UNEXPECTED_ERROR, "Update Error");
		} catch (FaroUnauthorizedException e) {
			throw new FaroWebAppException(FaroResponseStatus.UNAUTHORIZED, e.getMessage());
		}
		return JResponse.ok(updateResponse).status(Response.Status.OK).build();
	}
	
	@Path(GROUP_ID_PATH_PARAM_STRING+GROUP_DETAILS_PATH_CONST)
	@GET
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public JResponse<Group> details(@PathParam(GROUP_ID_PATH_PARAM) final String groupId){
		String userId = context.getUserPrincipal().getName();
		Group group;
		try {
			group = GroupManagement.getGroupDetails(groupId, userId);
		} catch (DataNotFoundException e) {
			throw new FaroWebAppException(FaroResponseStatus.NOT_FOUND, "Data Not Found");
		} catch (FaroUnauthorizedException e) {
			throw new FaroWebAppException(FaroResponseStatus.UNAUTHORIZED, e.getMessage());
		}
		return JResponse.ok(group).status(Response.Status.OK).build();
	}
}
	
