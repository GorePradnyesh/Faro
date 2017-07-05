package com.zik.faro.applogic;

import com.google.common.collect.Lists;
import com.zik.faro.data.FaroImageBase;
import com.zik.faro.persistence.datastore.FaroImageDatastoreImpl;
import com.zik.faro.persistence.datastore.data.FaroImageDo;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Created by granganathan on 12/30/16.
 */
public class ImageManagement {
    private static final Logger logger = Logger.getLogger(ImageManagement.class);

    public static FaroImageBase createImage(FaroImageBase faroImage) {
        FaroImageDo faroImageDo = ConversionUtils.toDo(faroImage);

        logger.info("Storing faroImageDo to db faroImageDo : " + faroImageDo);

        FaroImageDatastoreImpl.storeImage(faroImageDo);
        return ConversionUtils.fromDo(faroImageDo);
    }

    public static void deleteImage(String imageId, String eventId) {
        FaroImageDatastoreImpl.deleteImageById(imageId, eventId);
    }

    public static List<FaroImageBase> getImages(String eventId) {
        List<FaroImageBase> faroImages = Lists.newArrayList();

        for (FaroImageDo imageDo : FaroImageDatastoreImpl.loadImagesById(eventId)) {
            faroImages.add(ConversionUtils.fromDo(imageDo));
        }

        return faroImages;
    }
}
