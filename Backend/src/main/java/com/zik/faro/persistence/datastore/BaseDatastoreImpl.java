package com.zik.faro.persistence.datastore;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;

import com.zik.faro.persistence.datastore.data.BaseEntityDo;

public class BaseDatastoreImpl {
	public static boolean isVersionOk(BaseEntityDo toBeUpdated, BaseEntityDo existing){
		if(toBeUpdated.getVersion()!= null 
				&& !toBeUpdated.getVersion().equals(existing.getVersion())){
			return false;
		}
		return true;
	}
	
	public static void versionIncrement(BaseEntityDo toBeUpdated, BaseEntityDo existing){
		Long updatedVersion = toBeUpdated.getVersion();
		existing.setVersion(++updatedVersion);
	}
	

	public static void updateModifiedFields(Object originalObject, Object updateObject, Set<String> changedFields)
            throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        for (String field : changedFields) {
            BeanUtils.copyProperty(originalObject, field, PropertyUtils.getProperty(updateObject, field));
        }
    }
}
