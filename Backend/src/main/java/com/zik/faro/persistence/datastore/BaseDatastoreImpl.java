package com.zik.faro.persistence.datastore;

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
}
