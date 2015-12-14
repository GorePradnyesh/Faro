package com.zik.faro.persistence.datastore.data.user;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;
import com.zik.faro.data.IllegalDataOperation;

@Entity
public class FriendRelationDo {
    @Id
    private String toId;
    @Parent @Index
    private Ref<FaroUserDo> fromRef;

    // NOTE : This is in a de-normalized form to reduce the queries needed to fetch friends information for a particular user
    private String toFName;
    private String toLName;
    private String toExternalExpenseId;

    private FriendRelationDo(){}

    public FriendRelationDo(final String fromId,
                            final String toId,
                            final String toFName,
                            final String toLName,
                            final String toExternalExpenseId) throws IllegalDataOperation {
        if(fromId.equals(toId)){
            throw new IllegalDataOperation("Cannot establish a friend relation to self : " + this.toId);
        }
        this.toId = toId;
        this.fromRef = Ref.create(Key.create(FaroUserDo.class, fromId));
        this.toExternalExpenseId = toExternalExpenseId;
        this.toFName = toFName;
        this.toLName = toLName;
    }

    public String getFromId(){
        return this.fromRef.getKey().getName();
    }

    public FaroUserDo getFromUser(){
        return this.fromRef.get();
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
