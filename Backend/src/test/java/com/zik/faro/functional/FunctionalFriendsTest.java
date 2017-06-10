package com.zik.faro.functional;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.zik.faro.TestFaroUser;
import com.zik.faro.TestHelper;
import com.zik.faro.data.MinUser;
import org.assertj.core.util.Lists;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.net.URL;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by gaurav on 6/4/17.
 */
public class FunctionalFriendsTest {
    private static URL endpoint;
    private static TestFaroUser testFaroUser;
    private static TestFaroUser facebookFriendUser;
    private static final String FRIENDS_URL = "v1/friends";
    private static final String FB_FRIENDS_URL = "v1/friends/fbFriendsInvite";

    @BeforeClass
    public static void init() throws Exception {
        endpoint = TestHelper.getExternalTargetEndpoint();

        TestHelper.initializeFirebaseAdminSdk();
        testFaroUser = TestHelper.createTestFaroUser();
        facebookFriendUser = TestHelper.createTestFaroUser();

        assertThat(testFaroUser).isNotNull();
        assertThat(facebookFriendUser).isNotNull();
    }

    @AfterClass
    public static void cleanup() throws Exception {

    }

    // TODO : Complete this test
    @Ignore
    @Test
    public void friendsInviteTest() throws Exception {
        // Get friends
        ClientResponse response = TestHelper.doGET(endpoint.toString(), FRIENDS_URL, new MultivaluedMapImpl(), testFaroUser.getToken());

        assertThat(response).isNotNull();
        List<MinUser> minUsers = response.getEntity(new GenericType<List<MinUser>>(){});
        assertThat(minUsers).isEmpty();

        // Invite facebookFriendUser
        List<String> fbUserIds = Lists.newArrayList(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        response = TestHelper.doPOST(endpoint.toString(), FB_FRIENDS_URL, testFaroUser.getToken(), fbUserIds);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        minUsers = response.getEntity(new GenericType<List<MinUser>>(){});
        assertThat(minUsers).isNotEmpty();

    }
}
