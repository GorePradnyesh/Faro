package com.zik.faro.commons;

import com.zik.faro.commons.exceptions.FaroWebAppException;

/**
 * Util class for parameter validation
 */
public class ParamValidation {

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
