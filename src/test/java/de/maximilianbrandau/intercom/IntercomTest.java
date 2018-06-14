package de.maximilianbrandau.intercom;

import de.maximilianbrandau.intercom.client.IntercomClient;
import de.maximilianbrandau.intercom.codec.IntercomByteBuf;
import de.maximilianbrandau.intercom.codec.IntercomCodec;
import de.maximilianbrandau.intercom.server.IntercomServer;
import org.junit.Assert;
import org.junit.Test;

import javax.net.ssl.SSLException;
import java.security.cert.CertificateException;
import java.util.concurrent.ExecutionException;

public class IntercomTest {

    @Test
    public void simpleCommunicationTest() throws CertificateException, SSLException, InterruptedException, ExecutionException {
        IntercomCodec<Integer[]> integerIntercomCodec = new IntercomCodec<Integer[]>() {
            @Override
            public void encode(Integer[] data, IntercomByteBuf buffer) {
                buffer.writeVarInt(data.length);
                for (Integer i : data) buffer.writeVarInt(i);
            }

            @Override
            public Integer[] decode(IntercomByteBuf buffer) {
                Integer[] data = new Integer[buffer.readVarInt()];
                for (int i = 0; i < data.length; i++) {
                    data[i] = buffer.readVarInt();
                }
                return data;
            }
        };
        IntercomServer<Integer[]> server = Intercom.server(8080).build(integerIntercomCodec);
        IntercomClient<Integer[]> client = Intercom.client("localhost", 8080).requestTimeout(5000).build(integerIntercomCodec);
        server.addHandler("sum", (request, response) -> {
            int sum = 0;
            for (Integer i : request.getData()) sum += i;
            response.setData(new Integer[]{sum});
            response.end();
        });
        for (int i = 0; i < 100; i++) {
            Integer[] a = new Integer[]{i + i};
            client.request("sum").data(new Integer[]{i, i}).send().thenAccept(response -> Assert.assertArrayEquals(response.getData(), a)).get();
        }
        client.close();
        server.close();
    }

}
