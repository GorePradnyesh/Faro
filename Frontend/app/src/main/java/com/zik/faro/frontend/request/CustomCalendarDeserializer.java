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
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
        try {
            cal.setTime(sdf.parse("Mon Mar 14 16:02:37 GMT 2011"));// all done
        } catch (ParseException e) {
            throw new JsonParseException(e);
        }
        return cal;
    }
}