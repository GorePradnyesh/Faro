package com.zik.faro.commons;

import com.zik.faro.commons.exceptions.BadRequestException;

public class ParamValidation {

    public static void validateSignature(final String signature){
        // TODO : Replace with actual Signature param validation OAuth/Trusted Advisor etc.
        if(signature == null || signature.isEmpty()){
            throw new BadRequestException("Need a valid signature");
        }
    }

    public static void genericQueryParamValidations(final String queryParam, final String queryParamName){
        if(queryParam == null || queryParam.isEmpty()){
            throw new BadRequestException(String.format("non-empty Query param %s needed", queryParamName));
        }
    }

}
