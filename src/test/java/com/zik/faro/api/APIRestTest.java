package com.zik.faro.api;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.test.framework.JerseyTest;
import org.junit.Test;

/**
 * Created by pgore on 12/8/14.
 */
public class APIRestTest extends JerseyTest {
    public APIRestTest(){
        super("com.zik.faro");
    }

    @Test
    public void sampleTest(){
        WebResource webResource = resource();
        System.out.println("Sample");
    }
}
