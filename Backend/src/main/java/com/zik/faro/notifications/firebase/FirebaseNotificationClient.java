package com.zik.faro.notifications.firebase;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zik.faro.auth.AuthFilter;
import com.zik.faro.commons.ConfigPropertiesUtil;
import com.google.gson.Gson;
import com.zik.faro.commons.Constants;
import com.zik.faro.notifications.NotificationClient;

public class FirebaseNotificationClient implements NotificationClient<FirebaseHTTPRequest,FirebaseHTTPResponse>{
	private Gson gson = new Gson();
	private static String authHeaderValue;
	private  static final Logger logger = LoggerFactory.getLogger(FirebaseNotificationClient.class);
	
	public FirebaseNotificationClient() {
		authHeaderValue = ConfigPropertiesUtil.getFirebaseAuthorizationKey();
	}
	
	@Override
	public FirebaseHTTPResponse send(FirebaseHTTPRequest t) throws Exception {
		String payload = gson.toJson(t);
		HttpURLConnection conn = doPost(payload, Constants.FCM_ENDPOINT+Constants.SEND_NOTIFICATION_PATH_CONST);
		FirebaseHTTPResponse resp = getResponseObject(conn);
	    return resp;	
	}

	@Override
	public FirebaseHTTPResponse subscribeToTopic(FirebaseHTTPRequest t) throws Exception {
		String payload = gson.toJson(t);
		HttpURLConnection conn = doPost(payload, Constants.IID_ENDPOINT + Constants.SUBSCRIBE_TOKEN_PATH);
		FirebaseHTTPResponse resp = getResponseObject(conn);
        return resp;
	}
	
	@Override
	public FirebaseHTTPResponse unsubscribeToTopic(FirebaseHTTPRequest t) throws Exception {
		String payload = gson.toJson(t);
		HttpURLConnection conn = doPost(payload, Constants.IID_ENDPOINT + Constants.UNSUBSCRIBE_TOKEN_PATH);
		FirebaseHTTPResponse resp = getResponseObject(conn);
        return resp;
	}
	
	private HttpURLConnection doPost(String payload, String endpoint) throws Exception{
		
		logger.info("Invoking firebase");
		logger.info(MessageFormat.format("Payload: {0}", payload));
		logger.info(MessageFormat.format("Endpoint: {0}", endpoint));
		URL url = new URL(endpoint);
	    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	    conn.setDoOutput(true);
	    // Not sure if this will be honored since there is no way to set HTTP/1.1 version
	    conn.addRequestProperty("Connection", "keep-alive");
	    conn.addRequestProperty("Content-Type", "application/json");
	    // TODO: Hard coding for now
	    conn.addRequestProperty(Constants.AUTHORIZATION_HEADER_KEY, authHeaderValue);
	    conn.setRequestMethod("POST");
	    
	    OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
	    if(payload != null && !payload.isEmpty()){
	    	writer.write(payload);
	    }
	    writer.close();
	    return conn;
	}
	
	private FirebaseHTTPResponse getResponseObject(HttpURLConnection conn) throws Exception {
		int respCode = conn.getResponseCode();
	    String response = getResponseString(conn);
	    logger.info(MessageFormat.format("ResponseCode: {0}",respCode));
	    logger.info(MessageFormat.format("Response: {0}",response));
	    FirebaseHTTPResponse resp = gson.fromJson(response, FirebaseHTTPResponse.class);
	    resp.setStatusCode(respCode);
	    return resp;
	}
	
	private String getResponseString(HttpURLConnection conn) throws Exception{
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
	    String line = null;
        StringBuilder sb = new StringBuilder();

        while ((line = bufferedReader.readLine()) != null) {
            sb.append(line);
        }

        bufferedReader.close();
        String result = sb.toString();
        return result;
	}
	
}
