package com.zik.faro;

import org.codehaus.jackson.map.ObjectMapper;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;

public class TestHelper {
    /*Private helper functions*/
    //TODO: use Entity writers to get a cleaner flow, without writing marshal functions.

    private static ObjectMapper mapper = new ObjectMapper();
    private static final String HOSTNAME_PROPERTY = "testHostname";
    private static final String PORT_PROPERTY = "port";

    public static String getJsonRep(Object object) throws IOException {
        return mapper.writeValueAsString(object);
    }

    public static String getXMLRep(Object object, Class clazz) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(clazz);
        StringWriter stringWriter = new StringWriter();
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.marshal(object, stringWriter);
        String xmlRep = stringWriter.toString();
        return xmlRep;
    }


    public static URL getExternalTargetEndpoint() throws MalformedURLException {
        String hostname = System.getProperty(HOSTNAME_PROPERTY);
        if(hostname == null){
            hostname = "localhost";
        }
        String port = System.getProperty(PORT_PROPERTY);
        if(port == null){
            port = "8080";
        }
        return new URL("http://" + hostname + ":" + port);
    }
}
