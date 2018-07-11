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

/**
 * Encodes and decodes data
 *
 * @param <T> Data type to encode and decode
 */
public interface IntercomCodec<T> {

    /**
     * Encodes {@link T} into the given {@link IntercomByteBuf}
     *
     * @param data   Data to encode
     * @param buffer {@link IntercomByteBuf} that the data will be written to
     */
    void encode(T data, IntercomByteBuf buffer);

    /**
     * Decodes data from the given {@link IntercomByteBuf}
     *
     * @param buffer {@link IntercomByteBuf} that the data will be read from
     * @return The decoded data
     */
    T decode(IntercomByteBuf buffer);

}
