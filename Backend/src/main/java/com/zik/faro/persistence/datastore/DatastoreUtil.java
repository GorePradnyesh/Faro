package com.zik.faro.persistence.datastore;

import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.commons.exceptions.DatastoreException;

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
	
	public static void processResult(TransactionResult result) throws DatastoreException, DataNotFoundException{
		if(result == null){
			throw new DatastoreException("Transactional operation failed. Returned null response");
		}
		if(result.getStatus().equals(TransactionStatus.DATANOTFOUND)){
			throw new DataNotFoundException("Data not found");
		}
	}
}
