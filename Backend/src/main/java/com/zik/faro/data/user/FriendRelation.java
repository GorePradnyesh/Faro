package com.zik.faro.data.user;

import com.zik.faro.data.IllegalDataOperation;

public class FriendRelation {
    private String toId;
    private String fromId;

    // NOTE : This is in a de-normalized form to reduce the queries needed to fetch friends information for a particular user
    private String toFName;
    private String toLName;
    private String toExternalExpenseId;

    private FriendRelation(){}

    public FriendRelation(final String fromId,
                            final String toId,
                            final String toFName,
                            final String toLName,
                            final String toExternalExpenseId) throws IllegalDataOperation {
        if(fromId.equals(toId)){
            throw new IllegalDataOperation("Cannot establish a friend relation to self : " + this.toId);
        }
        this.toId = toId;
        this.fromId = fromId;
        this.toExternalExpenseId = toExternalExpenseId;
        this.toFName = toFName;
        this.toLName = toLName;
    }

    public String getFromId(){
        return this.fromId;
    }

    public String getToId(){
        return this.toId;
    }

    public String getToFName() {
        return toFName;
    }

    public String getToLName() {
        return toLName;
    }

    public String getToExternalExpenseId() {
        return toExternalExpenseId;
    }


}
