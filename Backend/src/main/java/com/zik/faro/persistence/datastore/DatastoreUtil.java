package com.zik.faro.persistence.datastore;

import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.DatastoreException;
import com.zik.faro.commons.exceptions.UpdateException;
import com.zik.faro.commons.exceptions.UpdateVersionException;

public class DatastoreUtil {
	public static TransactionStatus processResult(TransactionStatus result) throws DatastoreException, DataNotFoundException{
		if(result == null){
			throw new DatastoreException("Transactional operation failed. Returned null response");
		}
		if(result.equals(TransactionStatus.DATANOTFOUND)){
			throw new DataNotFoundException("Data not found");
		}
		return result;
	}
	
	public static void processResult(TransactionResult result) throws DatastoreException, DataNotFoundException, 
		UpdateVersionException, UpdateException{
		if(result == null){
			throw new DatastoreException("Transactional operation failed. Returned null response");
		}
		
		switch (result.getStatus()) {
			case DATANOTFOUND:
				throw new DataNotFoundException("Data not found");
			case VERSIONMISSMATCH:
				throw new UpdateVersionException(result.getMessage());
			case UPDATEEXCEPTION:
				throw new UpdateException(result.getMessage());
			default:
				break;
		}
	}
	
}
