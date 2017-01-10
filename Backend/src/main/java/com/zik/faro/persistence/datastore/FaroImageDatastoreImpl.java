package com.zik.faro.persistence.datastore;

import com.zik.faro.commons.exceptions.DataNotFoundException;
import com.zik.faro.persistence.datastore.data.EventDo;
import com.zik.faro.persistence.datastore.data.FaroImageDo;

import java.util.List;


/**
 * Created by granganathan on 12/30/16.
 */
public class FaroImageDatastoreImpl {

    public static void storeImage(final FaroImageDo faroImageDo){
        //TODO: Ensure that eventId exists before storing the image for that EventID
        DatastoreObjectifyDAL.storeObject(faroImageDo);
    }

    public static FaroImageDo loadImageById(final String imageName, final String eventId) throws DataNotFoundException {
        return DatastoreObjectifyDAL.loadObjectWithParentId(EventDo.class, eventId, FaroImageDo.class, imageName);
    }


    //NOTE: Since the images contain the INDEXED event id this function is placed in the FaroImageDatastoreImpl
    public static List<FaroImageDo> loadImagesById(final String eventId){
        return  DatastoreObjectifyDAL.loadObjectsByAncestorRef(EventDo.class, eventId, FaroImageDo.class);
    }

    public static void deleteImageById(final String imageName, final String eventId){
        DatastoreObjectifyDAL.deleteObjectByIdWithParentId(imageName, FaroImageDo.class, eventId, EventDo.class);
    }
}
