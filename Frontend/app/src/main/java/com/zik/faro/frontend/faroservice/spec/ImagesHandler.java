package com.zik.faro.frontend.faroservice.spec;

import com.zik.faro.data.FaroImageBase;
import com.zik.faro.frontend.faroservice.Callbacks.BaseFaroRequestCallback;

import java.util.List;

/**
 * Created by granganathan on 1/9/17.
 */

public interface ImagesHandler {
    void getImages(BaseFaroRequestCallback<List<FaroImageBase>> callback, String eventId);
    void createImages(BaseFaroRequestCallback<List<FaroImageBase>> callback, String eventId, List<FaroImageBase> images);
    void deleteImages(BaseFaroRequestCallback<String> callback, String eventId, String imageName);
}
