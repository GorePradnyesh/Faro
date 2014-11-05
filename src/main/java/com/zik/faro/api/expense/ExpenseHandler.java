package com.zik.faro.api.expense;

import com.sun.jersey.api.JResponse;
import com.zik.faro.api.responder.MinUser;
import com.zik.faro.commons.Constants;
import com.zik.faro.commons.ParamValidation;
import com.zik.faro.data.expense.ExpenseGroup;
import com.zik.faro.data.user.FaroUserName;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

@Path(Constants.EVENT_ID_PATH_CONST)
public class ExpenseHandler {
    @Path(Constants.EVENT_ID_PATH_PARAM_STRING + "/" + Constants.EXPENSE_ID_PATH_CONST)
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public JResponse<List<MinUser>> getExpenseIds(
                            @QueryParam(Constants.SIGNATURE_QUERY_PARAM) final String signature,
                            @PathParam(Constants.EVENT_ID_PATH_PARAM) final String eventId)
    {
        ParamValidation.validateSignature(signature);
        // TODO: ensure that eventId is valid

        final List<MinUser> friendList = new ArrayList<>();

        //TODO: Replace below dummy static response with actual code
        friendList.add(new MinUser(new FaroUserName("David","Gilmour"),"dg@dgdg.com", "dg@splitwise.com"));
        friendList.add(new MinUser(new FaroUserName("Roger","Waters"),"rw@dgdg.com", "rw@splitwise.com"));

        return JResponse.ok(friendList).build();
    }


    // This needs Content-Type to be specified.
    /*
    Sample payload
    <?xml version="1.0" encoding="UTF-8"?>
    <expenseGroup>
        <groupId>shasta123</groupId>
        <groupName>Shasta</groupName>
    </expenseGroup>
    */

    @Path(Constants.EVENT_ID_PATH_PARAM_STRING + "/" + Constants.CREATE_EXPENSE_GROUP_PATH_CONST)
    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public JResponse<String> createExpenseGroup(
            @QueryParam(Constants.SIGNATURE_QUERY_PARAM) final String signature, final ExpenseGroup expenseGroup)
    {
        ParamValidation.validateSignature(signature);
        ParamValidation.genericParamValidations(expenseGroup, "expenseGroup");
        // TODO: ensure that expense Group is valid

        //TODO: Add Business logic calls instead of directly responding with OK
        return JResponse.ok(Constants.HTTP_OK).build();
    }

}
