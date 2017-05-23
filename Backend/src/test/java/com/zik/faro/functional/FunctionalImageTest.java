package com.zik.faro.functional;

import com.google.common.collect.Lists;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.zik.faro.TestHelper;
import com.zik.faro.auth.jwt.FaroJwtTokenManager;
import com.zik.faro.data.FbImage;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URL;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by granganathan on 12/30/16.
 */
public class FunctionalImageTest {
    private static URL endpoint;
    private static String token = null;
    private static String eventId = null;

    @BeforeClass
    public static void init() throws Exception {
        endpoint = TestHelper.getExternalTargetEndpoint();
        token = TestHelper.createUserAndGetToken();
        eventId = TestHelper.createEventForTest(token, endpoint);
    }

    @AfterClass
    public static void cleanup() throws Exception {
        if (eventId != null) {
            TestHelper.deleteEvent(token, endpoint, eventId);
        }
    }

    @Test
    public void imagesTest() throws Exception {
        // Get images for the event and verify the list is empty
        ClientResponse response = TestHelper.doGET(endpoint.toString(),  getUrlForImages(), new MultivaluedMapImpl(), token);
        assertThat(response.getStatus()).isEqualTo(ClientResponse.Status.OK.getStatusCode());

        List<FbImage> imageList = response.getEntity(new GenericType<List<FbImage>>(){});
        assertThat(imageList).isEmpty();

        // Create images
        FbImage fbImage1 = new FbImage()
                .withEventId(eventId)
                .withImageName("randomPic001.jpg")
                .withFaroUserId(FaroJwtTokenManager.obtainClaimsWithNoChecks(token).getEmail())
                .withAlbumName(eventId)
                .withPublicUrl(new URL("https://something.com/randomPic001.jpg"));

        response = TestHelper.doPOST(endpoint.toString(), getUrlForImagesCreation(), token,
                Lists.newArrayList(fbImage1));
        assertThat(response.getStatus()).isEqualTo(ClientResponse.Status.OK.getStatusCode());

        imageList = response.getEntity(new GenericType<List<FbImage>>(){});
        assertThat(imageList).hasSize(1);

        response = TestHelper.doGET(endpoint.toString(),  getUrlForImages(), new MultivaluedMapImpl(), token);
        assertThat(response.getStatus()).isEqualTo(ClientResponse.Status.OK.getStatusCode());

        imageList = response.getEntity(new GenericType<List<FbImage>>(){});
        assertThat(imageList).hasSize(1);

        // Add more images
        FbImage fbImage2 = (FbImage) new FbImage()
                .withEventId(eventId)
                .withImageName("randomPic002.jpg")
                .withFaroUserId(FaroJwtTokenManager.obtainClaimsWithNoChecks(token).getEmail())
                .withAlbumName(eventId)
                .withPublicUrl(new URL("https://something.com/randomPic002.jpg"));

        FbImage fbImage3 = (FbImage) new FbImage()
                .withEventId(eventId)
                .withImageName("randomPic003.jpg")
                .withFaroUserId(FaroJwtTokenManager.obtainClaimsWithNoChecks(token).getEmail())
                .withAlbumName(eventId)
                .withPublicUrl(new URL("https://something.com/randomPic003.jpg"));

        response = TestHelper.doPOST(endpoint.toString(), getUrlForImagesCreation(), token,
                Lists.newArrayList(fbImage2, fbImage3));
        assertThat(response.getStatus()).isEqualTo(ClientResponse.Status.OK.getStatusCode());

        // Get list of images and verify all images are returned
        response = TestHelper.doGET(endpoint.toString(),  getUrlForImages(), new MultivaluedMapImpl(), token);
        assertThat(response.getStatus()).isEqualTo(ClientResponse.Status.OK.getStatusCode());
        imageList = response.getEntity(new GenericType<List<FbImage>>(){});

        assertThat(imageList).hasSize(3);
        assertThat(imageList).extracting("imageName").containsExactly("randomPic001.jpg", "randomPic002.jpg", "randomPic003.jpg");


        // Delete some images
        response = TestHelper.doPOST(endpoint.toString(), getUrlForImages(), token,
                Lists.newArrayList(fbImage1.getImageName()));
        assertThat(response.getStatus()).isEqualTo(ClientResponse.Status.OK.getStatusCode());

        // Get list of images and verify the deleted images are no longer present
        response = TestHelper.doGET(endpoint.toString(),  getUrlForImages(), new MultivaluedMapImpl(), token);
        assertThat(response.getStatus()).isEqualTo(ClientResponse.Status.OK.getStatusCode());
        imageList = response.getEntity(new GenericType<List<FbImage>>(){});
        assertThat(imageList).hasSize(2);
        assertThat(imageList).extracting("imageName").containsExactly("randomPic002.jpg", "randomPic003.jpg");

        // Delete remaining images
        response = TestHelper.doPOST(endpoint.toString(), getUrlForImages(), token,
                Lists.newArrayList(fbImage2.getImageName(), fbImage3.getImageName()));
        assertThat(response.getStatus()).isEqualTo(ClientResponse.Status.OK.getStatusCode());

        // Get list of images and verify the list is empty
        response = TestHelper.doGET(endpoint.toString(),  getUrlForImages(), new MultivaluedMapImpl(), token);
        assertThat(response.getStatus()).isEqualTo(ClientResponse.Status.OK.getStatusCode());
        imageList = response.getEntity(new GenericType<List<FbImage>>(){});
        assertThat(imageList).isEmpty();
    }

    private String getUrlForImages() {
        return "v1/event/" + eventId + "/images";
    }

    private String getUrlForImagesCreation() {
        return "v1/event/" + eventId + "/images/create";
    }

    private String getUrlForImagesDeletion() {
        return "v1/event/" + eventId + "/images";
    }
}
