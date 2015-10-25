package com.zik.faro;

import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;

public class RestClient {
	private static RestClient instance = new RestClient();
	private Client client = null;
	
	
	private RestClient(){
		ClientConfig config = new DefaultClientConfig();
	    config.getClasses().add(JacksonJaxbJsonProvider.class);
	    config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
		this.client = Client.create(config);
	}
	
	public static RestClient getInstance(){
		return instance;
	}
	
	public Client getClient(){
		return this.client;
	}
}
