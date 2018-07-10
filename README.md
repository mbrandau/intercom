# intercom ![Maven Central](https://img.shields.io/maven-central/v/de.maximilianbrandau/intercom.svg) [![Build Status](https://img.shields.io/travis/mbrandau/intercom.svg)](https://travis-ci.org/mbrandau/intercom)

Intercom is a simple library that aims to simplify communication between your services.

### Prerequisites

- [Apache Maven](https://maven.apache.org/)
- Java Version 9

## Quickstart

1. Add the intercom dependency to your `pom.xml`
    ```xml
    ...
    <dependencies>
        <dependency>
            <groupId>de.maximilianbrandau</groupId>
            <artifactId>intercom</artifactId>
            <version>0.0.1</version>
        </dependency>
    </dependencies>
    ...
    ```

2. Create an `IntercomCodec`
    ```java
    import de.maximilianbrandau.intercom.codec.IntercomByteBuf;
    import de.maximilianbrandau.intercom.codec.IntercomCodec;
    ```
    ```java
    IntercomCodec<Integer[]> integerIntercomCodec = new IntercomCodec<>() {
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
    ```

3. Create an `IntercomServer`
    ```java
    import de.maximilianbrandau.intercom.server.IntercomServer;
    ```
    ```java
    IntercomServer<Integer[]> server = Intercom.server(8080).build(integerIntercomCodec); // Start the server with your codec
    server.addHandler("sum", (request, response) -> {
        int sum = 0;
        for (Integer i : request.getData()) sum += i; // Sum up all values in the integer array
        response.setData(new Integer[]{sum}); // Set the result
        response.end(); // End the response
    });
    ```

4. Create an `IntercomClient`
    ```java
    import de.maximilianbrandau.intercom.client.IntercomClient;
    ```
    ```java
    IntercomClient<Integer[]> client = Intercom.client("localhost", 8080).build(integerIntercomCodec);
    client.request("sum").data(new Integer[]{5, 7, 9}).send() // Send the request
        .thenAccept(response -> System.out.println(response.getData()[0])).get(); // Print the response
    ```

5. Close client and server once you're done
    ```java
    client.close();
    server.close();
    ```