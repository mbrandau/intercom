package de.maximilianbrandau.intercom;

import de.maximilianbrandau.intercom.client.IntercomClient;
import de.maximilianbrandau.intercom.codec.StringEncodingMechanism;
import de.maximilianbrandau.intercom.server.IntercomServer;
import org.junit.Assert;
import org.junit.Test;

import javax.net.ssl.SSLException;
import java.security.cert.CertificateException;

public class IntercomTest {

    @Test
    public void simpleCommunicationTest() throws CertificateException, SSLException {
        IntercomServer<String> server = Intercom.server(8080).build(new StringEncodingMechanism());
        IntercomClient<String> client = Intercom.client("localhost", 8080).build(new StringEncodingMechanism());
        server.addHandler("testEvent", (request, response) -> {
            response.setData(request.getData().toUpperCase());
            response.end();
        });
        client.request("testEvent").data("test_string").response(data -> {
            Assert.assertEquals(data.getData(), "TEST_STRING");
            client.close();
            server.close();
        }).build();
    }

}
