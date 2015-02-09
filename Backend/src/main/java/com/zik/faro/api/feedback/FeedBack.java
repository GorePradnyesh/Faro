package com.zik.faro.api.feedback;

import com.zik.faro.commons.Constants;
import com.zik.faro.commons.ParamValidation;
import com.zik.faro.commons.exceptions.BadRequestException;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

@Path(Constants.FEEDBACK_PATH_CONST)
public class FeedBack {

    private static final int MAX_FEEDBACK_STRING_LENGTH = 2000;

    @POST
    public String sendFeedback(@QueryParam(Constants.SIGNATURE_QUERY_PARAM) final String signature,
                             final String feedbackString){
        ParamValidation.validateSignature(signature);
        ParamValidation.genericParamValidations(feedbackString, "feedbackString");
        if(feedbackString.length() > MAX_FEEDBACK_STRING_LENGTH){
            throw new BadRequestException("FeedBack String length exceeds max size : " + MAX_FEEDBACK_STRING_LENGTH);
        }
        return Constants.HTTP_OK;
    }
}
