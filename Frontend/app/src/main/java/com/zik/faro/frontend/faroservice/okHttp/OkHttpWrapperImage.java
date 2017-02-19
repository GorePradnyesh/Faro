package com.zik.faro.frontend.faroservice.okHttp;

import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.zik.faro.data.FaroImageBase;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;
import com.zik.faro.frontend.faroservice.auth.TokenCache;
import com.zik.faro.frontend.faroservice.spec.ImagesHandler;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by granganathan on 1/9/17.
 */

public class OkHttpWrapperImage extends BaseFaroOKHttpWrapper implements ImagesHandler {
    public OkHttpWrapperImage(URL baseUrl) {
        super(baseUrl, "event");
    }

    @Override
    public void getImages(BaseFaroRequestCallback<List<FaroImageBase>> callback, String eventId) {
        String token = TokenCache.getTokenCache().getToken();
        if(token != null){
            Request request = new Request.Builder()
                    .url(this.baseHandlerURL.toString() + eventId + "/images")
                    .addHeader(authHeaderName, token)
                    .build();
            Type imagesList = new TypeToken<ArrayList<FaroImageBase>>(){}.getType();
            this.httpClient.newCall(request).enqueue(new DeserializerHttpResponseHandler<List<FaroImageBase>>(callback, imagesList));
        } else {
            callback.onFailure(null, new IOException("Could not fetch auth token"));
        }
    }

    @Override
    public void createImages(BaseFaroRequestCallback<List<FaroImageBase>> callback, String eventId, List<FaroImageBase> images) {
        String token = TokenCache.getTokenCache().getToken();
        if(token != null) {
            Request request = new Request.Builder()
                    .url(this.baseHandlerURL.toString() + eventId + "/images/create")
                    .addHeader(authHeaderName, token)
                    .post(RequestBody.create(MediaType.parse(DEFAULT_CONTENT_TYPE), mapper.toJson(images)))
                    .build();
            Type imagesList = new TypeToken<ArrayList<FaroImageBase>>(){}.getType();
            this.httpClient.newCall(request).enqueue(new DeserializerHttpResponseHandler<List<FaroImageBase>>(callback, imagesList));
        } else {
            callback.onFailure(null, new IOException("Could not fetch auth token"));
        }
    }

    @Override
    public void deleteImages(BaseFaroRequestCallback<String> callback, String eventId, String imageName) {

    }
}
