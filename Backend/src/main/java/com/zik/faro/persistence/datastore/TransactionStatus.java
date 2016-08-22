package com.zik.faro.persistence.datastore;

// Cannot throw an Exception(in case if entity is not found in datastore)
// while within a transaction. Require something to identify failures
public enum TransactionStatus {
	SUCCESS,
	DATANOTFOUND;
}
