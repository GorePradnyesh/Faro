package com.zik.faro.data.user;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;
import com.zik.faro.commons.exceptions.IllegalDataOperation;

@Entity
public class FriendRelation {
    @Id
    private String toId;
    @Parent @Index
    private Ref<FaroUser> fromRef;

    public int tempVal;

    public FriendRelation(final String fromId, final String toId) throws IllegalDataOperation {
        if(fromId.equals(toId)){
            throw new IllegalDataOperation("Cannot establish a friend relation to self : " + this.toId);
        }
        this.toId = toId;
        this.fromRef = Ref.create(Key.create(FaroUser.class, fromId));
    }

    public String getFromId(){
        return this.fromRef.getKey().getName();
    }

    public FaroUser getFromUser(){
        return this.fromRef.get();
    }

    public String getToId(){
        return this.getToId();
    }

}
