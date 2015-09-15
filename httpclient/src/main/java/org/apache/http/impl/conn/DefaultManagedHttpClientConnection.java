/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.http.impl.conn;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.Socket;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import com.santaba.common.logger.LogMsg;
import com.santaba.sitemonitor.util.httpclientnew.SMMetrics;
import org.apache.http.*;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.config.MessageConstraints;
import org.apache.http.conn.ManagedHttpClientConnection;
import org.apache.http.entity.ContentLengthStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.DefaultBHttpClientConnection;
import org.apache.http.io.HttpMessageParserFactory;
import org.apache.http.io.HttpMessageWriterFactory;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

/**
 * Default {@link ManagedHttpClientConnection} implementation.
 * @since 4.3
 */
@NotThreadSafe
public class DefaultManagedHttpClientConnection extends DefaultBHttpClientConnection
                                 implements ManagedHttpClientConnection, HttpContext {

    private final String id;
    private final Map<String, Object> attributes;

    private volatile boolean shutdown;

    public DefaultManagedHttpClientConnection(
            final String id,
            final int buffersize,
            final int fragmentSizeHint,
            final CharsetDecoder chardecoder,
            final CharsetEncoder charencoder,
            final MessageConstraints constraints,
            final ContentLengthStrategy incomingContentStrategy,
            final ContentLengthStrategy outgoingContentStrategy,
            final HttpMessageWriterFactory<HttpRequest> requestWriterFactory,
            final HttpMessageParserFactory<HttpResponse> responseParserFactory) {
        super(buffersize, fragmentSizeHint, chardecoder, charencoder,
                constraints, incomingContentStrategy, outgoingContentStrategy,
                requestWriterFactory, responseParserFactory);
        this.id = id;
        this.attributes = new ConcurrentHashMap<String, Object>();
    }

    public DefaultManagedHttpClientConnection(
            final String id,
            final int buffersize) {
        this(id, buffersize, buffersize, null, null, null, null, null, null, null);
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void shutdown() throws IOException {
        this.shutdown = true;
        super.shutdown();
    }

    @Override
    public HttpResponse receiveResponseHeader() throws HttpException, IOException {
        SMMetrics.INSTANCE.setMetric(SMMetrics.FIRST_HEADER_READ_TIME, System.currentTimeMillis());
        HttpResponse response =  super.receiveResponseHeader();
        SMMetrics.INSTANCE.setMetric(SMMetrics.LAST_HEADER_READ_TIME, System.currentTimeMillis());

        return response;
    }

    @Override
    public void receiveResponseEntity(HttpResponse response) throws HttpException, IOException {
        super.receiveResponseEntity(response);

        ContentType contentType = ContentType.get(response.getEntity());
        String mime = null;
        String charset = null;
        if (contentType != null) {
            mime = contentType.getMimeType();
            charset = contentType.getCharset().name();
        }

        // manualy intercept response instead of use SMResponseInterceptor
        HttpEntity entity = response.getEntity();

        Header contentEncoding = entity.getContentEncoding();
        if (contentEncoding != null) {
            for (HeaderElement codec : contentEncoding.getElements()) {
                if (codec.getName().equalsIgnoreCase("gzip")) {
                    LogMsg.debug("Got gzip content");
                    response.setEntity(new GzipDecompressingEntity(response.getEntity()));
                }
            }
        }

        String body = EntityUtils.toString(response.getEntity(), "UTF-8");

        long epoch = System.currentTimeMillis();
        LogMsg.debug("SMconnection.receiveResponseEntity", String.format(
                "Finish read response at - %s. mime type = %s, charset = %s",
                epoch, mime, charset
        ));

        LogMsg.trace(String.format("HttpResponse Payload (%d bytes)-\n%s", body.length(), ""));

        SMMetrics.INSTANCE.setMetric(SMMetrics.LAST_ENTITY_READ_TIME, epoch, true);   // in sitemonitor's SMConnection, third param is not set.
        SMMetrics.INSTANCE.setMetric(SMMetrics.LAST_RESPONSE_BODY, body, true);
        SMMetrics.INSTANCE.setMetric(SMMetrics.LAST_RESPONSE_MIME, mime, true);
        SMMetrics.INSTANCE.setMetric(SMMetrics.LAST_RESPONSE_CHARSET, charset, true);
    }

    @Override
    public void sendRequestHeader(HttpRequest request) throws HttpException, IOException {
        LogMsg.debug("SMConnection.sendRequestHeader", "Sending request header: " + request.getRequestLine());

        super.sendRequestHeader(request);

        // it's possible there's no entitty (payload) to send

        long epoch = System.currentTimeMillis();
        LogMsg.debug("SMConnection.sendRequestHeader", "Finish sending request header at - " + epoch);

        SMMetrics.INSTANCE.setMetric(SMMetrics.LAST_HEADER_SENT_TIME, System.currentTimeMillis());

        Header[] headers = request.getAllHeaders();
        for (Header header : headers) {
            LogMsg.debug("SMConnection.sendRequestHeader", ">> " + header.toString());
        }
    }

    @Override
    public void sendRequestEntity(HttpEntityEnclosingRequest request) throws HttpException, IOException {
        LogMsg.debug("SMConnection.sendRequestEntity", "Sending request entity ...  ");

        super.sendRequestEntity(request);

        long epoch = System.currentTimeMillis();
        LogMsg.debug("SMConnection.sendRequestEntity", "Finish sending request entity at - " + epoch);
        SMMetrics.INSTANCE.setMetric(SMMetrics.LAST_ENTITY_SENT_TIME, epoch);
    }

    @Override
    public Object getAttribute(final String id) {
        return this.attributes.get(id);
    }

    @Override
    public Object removeAttribute(final String id) {
        return this.attributes.remove(id);
    }

    @Override
    public void setAttribute(final String id, final Object obj) {
        this.attributes.put(id, obj);
    }

    @Override
    public void bind(final Socket socket) throws IOException {
        if (this.shutdown) {
            socket.close(); // allow this to throw...
            // ...but if it doesn't, explicitly throw one ourselves.
            throw new InterruptedIOException("Connection already shutdown");
        }
        super.bind(socket);
    }

    @Override
    public Socket getSocket() {
        return super.getSocket();
    }

    @Override
    public SSLSession getSSLSession() {
        final Socket socket = super.getSocket();
        if (socket instanceof SSLSocket) {
            return ((SSLSocket) socket).getSession();
        } else {
            return null;
        }
    }

}
