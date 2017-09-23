package com.zik.faro.frontend;

import com.zik.faro.data.BaseEntity;
import com.zik.faro.frontend.util.FaroObjectNotFoundException;

public interface FaroObjectHandler<T> {
    boolean checkObjectVersionIfLatest (String objectId, Long cloneVersion) throws FaroObjectNotFoundException;
    T getOriginalObject (String objectId) throws FaroObjectNotFoundException;
    public T getCloneObject(String objectId) throws FaroObjectNotFoundException;
}
