package com.zik.faro.api.event;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;


//tell spring it's a provider (type is determined by the implements)
@Provider
public class ObjectMapperProvider implements ContextResolver<ObjectMapper> {
 @Override
 public ObjectMapper getContext(Class<?> type) {
     // create the objectMapper.
     ObjectMapper objectMapper = new ObjectMapper();
     // configure the object mapper here, eg.
        objectMapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
     return objectMapper;
 }
}
