package com.zik.faro.persistence.datastore;

/**
 * Created by pgore on 12/17/14.
 */
public enum DatastoreOperator {
    EQ("=="),
    GTE(">="),
    LTE("<="),
    GT(">"),
    LT("<"),
    IN("in"),
    NE("!=");

    private String operatorString;
    DatastoreOperator(final String operatorString){
        this.operatorString = operatorString;
    }

    @Override
    public String toString(){
        return this.operatorString;
    }

    public String toStringSpacePrefix(){
        return " " +this.operatorString;
    }
}
