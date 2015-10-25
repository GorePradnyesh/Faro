package com.zik.faro.frontend.request;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CustomCalendarDeserializer implements JsonDeserializer<Calendar> {
    public Calendar deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException{
        Calendar cal = Calendar.getInstance();
        String content = json.getAsJsonPrimitive().getAsString();
        //2015-10-18T21:58:39.283+0000
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        try {
            cal.setTime(sdf.parse(content));// all done
        } catch (ParseException e) {
            throw new JsonParseException(e);
        }
        return cal;
    }
}