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

package de.maximilianbrandau.intercom.codec;

public enum PacketType {

    /**
     * Authentication request used to authenticate the {@link de.maximilianbrandau.intercom.client.IntercomClient} at the {@link de.maximilianbrandau.intercom.server.IntercomServer}
     */
    AUTH((byte) 0),
    /**
     * The {@link de.maximilianbrandau.intercom.server.IntercomServer}s response to the authentication request
     */
    AUTH_RESPONSE((byte) 1),
    /**
     * Bidirectional ping packet
     */
    PING((byte) 10),
    /**
     * Bidirectional request packet
     */
    REQUEST((byte) 100),
    /**
     * Bidirectional packet
     * Send in response to the request packet
     */
    RESPONSE((byte) 101),
    /**
     * Used to push {@link de.maximilianbrandau.intercom.Event}s from the {@link de.maximilianbrandau.intercom.server.IntercomServer} to all {@link de.maximilianbrandau.intercom.client.IntercomClient}s
     */
    PUSH((byte) 120);

    private final byte id;

    PacketType(byte id) {
        this.id = id;
    }

    public static PacketType getById(byte id) {
        PacketType[] types = values();
        for (PacketType type : types) if (type.getId() == id) return type;
        return null;
    }

    public byte getId() {
        return id;
    }

}
