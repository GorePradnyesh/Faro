package com.zik.faro.commons;

/**
 * Created by pgore on 11/4/14.
 */
public final class Constants {

    public static final String HTTP_OK = "OK";

    public static final String SIGNATURE_QUERY_PARAM = "Signature";
    public static final String FARO_USER_ID_PARAM = "userId";

    public static final String EVENT_ID = "eventID";


    private static String getPathParamString(final String paramName){
        return String.format("{%s}", paramName);
    }


}
