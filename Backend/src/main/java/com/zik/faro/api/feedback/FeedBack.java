package com.zik.faro.api.feedback;

import com.zik.faro.commons.Constants;
import com.zik.faro.commons.FaroResponseStatus;
import com.zik.faro.commons.ParamValidation;
import com.zik.faro.commons.exceptions.FaroWebAppException;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

@Path(Constants.FEEDBACK_PATH_CONST)
public class FeedBack {
    private static final int MAX_FEEDBACK_STRING_LENGTH = 2000;

    @POST
    public String sendFeedback(final String feedbackString){
        ParamValidation.genericParamValidations(feedbackString, "feedbackString");
        if(feedbackString.length() > MAX_FEEDBACK_STRING_LENGTH){
            throw new FaroWebAppException(FaroResponseStatus.BAD_REQUEST, "FeedBack String length exceeds max size : " + MAX_FEEDBACK_STRING_LENGTH);
        }
        return Constants.HTTP_OK;
    }
}
