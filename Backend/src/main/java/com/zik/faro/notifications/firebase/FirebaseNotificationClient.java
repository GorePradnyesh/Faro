package com.zik.faro.notifications.firebase;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.client.HttpClient;

import com.google.gson.Gson;
import com.zik.faro.commons.Constants;
import com.zik.faro.notifications.NotificationClient;

public class FirebaseNotificationClient implements NotificationClient<FirebaseHTTPRequest,FirebaseHTTPResponse>{
	private Gson gson = new Gson();
	
	@Override
	public FirebaseHTTPResponse send(FirebaseHTTPRequest t) throws Exception {
		String payload = gson.toJson(t);
		HttpURLConnection conn = doPost(payload, Constants.FCM_ENDPOINT+Constants.SEND_NOTIFICATION_PATH_CONST);
		String response = getResponseString(conn);
	    FirebaseHTTPResponse resp = gson.fromJson(response, FirebaseHTTPResponse.class);
	    return resp;	
	}

	@Override
	public FirebaseHTTPResponse createTopic(FirebaseHTTPRequest t) throws Exception {
		String payload = gson.toJson(t);
		HttpURLConnection conn = doPost(payload, Constants.IID_ENDPOINT + Constants.CREATE_TOKEN_PATH_CONST_FIRST_PART);
		FirebaseHTTPResponse resp = getResponseObject(conn);
        return resp;
	}	
	
	private HttpURLConnection doPost(String payload, String endpoint) throws Exception{
		
		URL url = new URL(endpoint);
	    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	    conn.setDoOutput(true);
	    // Not sure if this will be honored since there is no way to set HTTP/1.1 version
	    conn.addRequestProperty("Connection", "keep-alive");
	    conn.addRequestProperty("Content-Type", "application/json");
	    // TODO: Hard coding for now
	    conn.addRequestProperty("Authorization", "key=AAAAb-Q4pn8:APA91bFsIiD5FRV2JfDp50FVRYpk3KlObZM4bwW9Zi6eO1UNVJ6G7OuJJQRK_c_Cr-oSnCGJBqUH6eRzL5bTx5GqY3r6IysvWLtmiZisQRFxmcx0eXJDnUJGiX-QTFJqRj4vQhxsieGQ");
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
	    System.out.println(response);
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
