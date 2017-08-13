package com.zik.faro.frontend;

import com.google.gson.Gson;
import com.zik.faro.data.BaseEntity;
import com.zik.faro.frontend.util.FaroObjectNotFoundException;

// Function returns true if the versions match
public abstract class BaseObjectHandler<T extends BaseEntity> implements FaroObjectHandler<T>{
    @Override
    public boolean checkObjectVersionIfLatest(String objectId, Long cloneVersion) throws FaroObjectNotFoundException{
        T originalBaseEntity = getOriginalObject(objectId);
        return cloneVersion.equals(originalBaseEntity.getVersion());
    }

    @Override
    public T getCloneObject(String objectId) throws FaroObjectNotFoundException {
        T originalBaseEntity = getOriginalObject(objectId);
        Gson gson = new Gson();
        String json = gson.toJson(originalBaseEntity);
        return gson.fromJson(json, getType());
    }

    public abstract Class<T> getType();
}
