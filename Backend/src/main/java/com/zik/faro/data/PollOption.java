package com.zik.faro.data;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.google.appengine.repackaged.com.google.common.base.Objects;

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
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PollOption other = (PollOption) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
