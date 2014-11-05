package com.zik.faro.commons;

import com.zik.faro.api.exceptions.BadRequestException;

public class ParamValidation {

    public static void validateSignature(final String signature){
        if(signature == null || signature.isEmpty()){
            System.out.println("Throwing new exception");
            throw new BadRequestException("Need a valid signature");
        }
    }

}
