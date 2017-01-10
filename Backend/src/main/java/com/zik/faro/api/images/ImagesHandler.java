package com.zik.faro.api.images;

import com.google.common.collect.Lists;
import com.sun.jersey.api.JResponse;
import com.zik.faro.applogic.ImageManagement;
import com.zik.faro.commons.Constants;
import com.zik.faro.data.FaroImageBase;
import org.apache.log4j.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static com.zik.faro.commons.Constants.EVENT_PATH_CONST;
import static com.zik.faro.commons.Constants.EVENT_ID_PATH_PARAM;
import static com.zik.faro.commons.Constants.EVENT_ID_PATH_PARAM_STRING;
import static com.zik.faro.commons.Constants.IMAGES_PATH_CONST;
import static com.zik.faro.commons.Constants.IMAGE_ID_PATH_PARAM;
import static com.zik.faro.commons.Constants.IMAGES_CREATE_CONST;
import static com.zik.faro.commons.Constants.IMAGE_ID_PATH_PARAM_STRING;


/**
 * Created by granganathan on 12/29/16.
 */
@Path(EVENT_PATH_CONST + EVENT_ID_PATH_PARAM_STRING + IMAGES_PATH_CONST)
public class ImagesHandler {
    private static final Logger logger = Logger.getLogger(ImagesHandler.class);

    @Path(IMAGE_ID_PATH_PARAM_STRING)
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public JResponse<FaroImageBase> getImage(@PathParam(EVENT_ID_PATH_PARAM) String eventId, @PathParam(IMAGE_ID_PATH_PARAM) String imageId) {
        return null;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public JResponse<List<FaroImageBase>> getImages(@PathParam(EVENT_ID_PATH_PARAM) String eventId) {
        List<FaroImageBase> faroImages = ImageManagement.getImages(eventId);
        logger.info("returning images : " + faroImages);
        return JResponse.ok(faroImages).build();
    }

    @Path(IMAGES_CREATE_CONST)
    @POST
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public JResponse<List<FaroImageBase>> createImages(@PathParam(EVENT_ID_PATH_PARAM) String eventId, List<FaroImageBase> images) {
        logger.info("create images " + images);
        List<FaroImageBase> createdImages = Lists.newArrayList();
        for (FaroImageBase faroImage : images) {
            createdImages.add(ImageManagement.createImage(faroImage));
        }

        return JResponse.ok(createdImages).build();
    }

    @POST
    public JResponse<String> deleteImages(@PathParam(EVENT_ID_PATH_PARAM) String eventId, List<String> imageIds) {
        for (String imageName : imageIds) {
            ImageManagement.deleteImage(imageName, eventId);
        }
        return JResponse.ok(Constants.HTTP_OK).build();
    }
}
