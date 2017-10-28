package de.maximilianbrandau.intercom;

import de.maximilianbrandau.intercom.client.IntercomClient;
import de.maximilianbrandau.intercom.client.IntercomResponseHandler;
import de.maximilianbrandau.intercom.encoding.StringEncodingMechanism;
import de.maximilianbrandau.intercom.server.IntercomServer;

import javax.net.ssl.SSLException;
import java.security.cert.CertificateException;

public class Intercom {

    public static IntercomClient.Builder client(String host, int port) {
        return new IntercomClient.Builder(host, port);
    }

    public static IntercomServer.Builder server(int port) {
        return new IntercomServer.Builder(port);
    }

    public static void main(String[] args) throws SSLException, CertificateException {
        server(8080).build(new StringEncodingMechanism()).addHandler("turnAround", (request, response) -> {
            response.setData(backwards(request.getData()));
            response.end();
        });

        client("localhost", 8080).build(new StringEncodingMechanism()).request("turnAround", "Test String to turn around.", new IntercomResponseHandler<String>() {
            @Override
            public void handleResponse(de.maximilianbrandau.intercom.client.IntercomResponse<String> response) {
                System.out.println(response.getData());
            }

            @Override
            public void handleError(Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }

    public static String backwards(String s) {
        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length / 2; i++) {
            char c = chars[i];
            chars[i] = chars[chars.length - 1 - i];
            chars[chars.length - 1 - i] = c;
        }
        return new String(chars);
    }

}
