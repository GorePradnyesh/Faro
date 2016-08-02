package com.zik.faro.data;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PollOption{
    private String id;                                 
    private String option;
    private Set<String> voters = new HashSet<String>();

    public PollOption(){
        
    }
    
    public PollOption(final String id, final String option){
    	this.setId(id);
        this.setOption(option);
    }

    public PollOption(final String option){
    	this(null, option);
    }

//    public List<String> getVotersAsList(){
//    	return Arrays.asList((String[])this.voters.toArray());                              
//    }
    
    public Set<String> getVoters(){
    	return this.voters;
    }

    public void addVoters(final String voterId){
        this.voters.add(voterId);
    }

    public void removeVoter(final String voterId) {this.voters.remove(voterId);}
    
    public void setVoters(final Set<String> voters){
    	this.voters = voters;
    }

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getOption() {
		return option;
	}

	public void setOption(String option) {
		this.option = option;
	}
}
