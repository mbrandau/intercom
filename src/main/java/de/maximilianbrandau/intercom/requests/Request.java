/*
 * Copyright (c) 2017-2018 Maximilian Brandau
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package de.maximilianbrandau.intercom.requests;

import de.maximilianbrandau.intercom.authentication.AuthenticationResult;

import java.net.InetSocketAddress;

public class Request<T> {

    private final long receiveTime;
    private final InetSocketAddress address;
    private final AuthenticationResult authenticationResult;
    private final int requestId;
    private final String event;
    private final T data;

    public Request(long receiveTime, InetSocketAddress address, AuthenticationResult authenticationResult, int requestId, String event, T data) {
        this.receiveTime = receiveTime;
        this.address = address;
        this.authenticationResult = authenticationResult;
        this.requestId = requestId;
        this.event = event;
        this.data = data;
    }

    public long getReceiveTime() {
        return receiveTime;
    }

    public InetSocketAddress getAddress() {
        return address;
    }

    public AuthenticationResult getAuthenticationResult() {
        return authenticationResult;
    }

    public int getRequestId() {
        return requestId;
    }

    public String getEvent() {
        return event;
    }

    public T getData() {
        return data;
    }

}
