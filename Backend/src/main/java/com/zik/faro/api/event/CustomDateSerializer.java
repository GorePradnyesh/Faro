package com.zik.faro.api.event;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.ser.std.SerializerBase;

public class CustomDateSerializer extends SerializerBase<Calendar> {

	public CustomDateSerializer() {
	    super(Calendar.class, true);
	}
	
	@Override
	public void serialize(Calendar value, JsonGenerator jgen, SerializerProvider provider)
	        throws IOException, JsonGenerationException {
	    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
	    String format = formatter.format(value.getTime());
	    jgen.writeString(format);
	}

}
