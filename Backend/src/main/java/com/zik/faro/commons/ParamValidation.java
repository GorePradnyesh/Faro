package com.zik.faro.commons;

import com.zik.faro.commons.exceptions.FaroWebAppException;

/**
 * Util class for parameter validation
 */
public class ParamValidation {

    public static void validateSignature(final String signature){
        // TODO : Replace with actual Signature param validation OAuth/Trusted Advisor etc.
        if(signature == null || signature.isEmpty()){
            throw new FaroWebAppException(FaroResponseStatus.BAD_REQUEST, "Need a valid signature");
        }
    }

    public static void genericParamValidations(final String param, final String queryParamName){
        if(param == null || param.isEmpty()){
            throw new FaroWebAppException(FaroResponseStatus.BAD_REQUEST, String.format("required : non-empty param:%s", queryParamName));
        }
    }

    public static void genericParamValidations(final Object param, final String queryParamName){
        if(param == null){
            throw new FaroWebAppException(FaroResponseStatus.BAD_REQUEST, String.format("required : non-empty param:%s", queryParamName));
        }
    }

}
