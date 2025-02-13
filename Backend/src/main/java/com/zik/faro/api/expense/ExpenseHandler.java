package com.zik.faro.api.expense;

import com.sun.jersey.api.JResponse;
import com.zik.faro.data.MinUser;
import com.zik.faro.commons.ParamValidation;
import com.zik.faro.data.expense.ExpenseGroup;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
import static com.zik.faro.commons.Constants.*;

@Path(EXPENSE_PATH_CONST + EVENT_ID_PATH_PARAM_STRING)
public class ExpenseHandler {
    @Path(EXPENSE_ID_PATH_CONST)
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public JResponse<List<MinUser>> getExpenseIds(@PathParam(EVENT_ID_PATH_PARAM) final String eventId)
    {
        // TODO: ensure that eventId is valid

        final List<MinUser> friendList = new ArrayList<>();

        //TODO: Replace below dummy static response with actual code
        friendList.add(new MinUser("David","Gilmour"+eventId,"dg@dgdg.com", "dg@splitwise.com"));
        friendList.add(new MinUser("Roger","Waters"+eventId,"rw@dgdg.com", "rw@splitwise.com"));

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

    @Path(CREATE_EXPENSE_GROUP_PATH_CONST)
    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public JResponse<String> createExpenseGroup(final ExpenseGroup expenseGroup)
    {
        ParamValidation.genericParamValidations(expenseGroup, "expenseGroup");
        // TODO: ensure that expense Group is valid

        //TODO: Add Business logic calls instead of directly responding with OK
        return JResponse.ok(HTTP_OK).build();
    }

}
