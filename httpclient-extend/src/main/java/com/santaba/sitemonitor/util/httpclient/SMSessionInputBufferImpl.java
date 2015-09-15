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

package com.santaba.sitemonitor.util.httpclient;

import com.santaba.common.logger.LogMsg;
import org.apache.http.MessageConstraintException;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.config.MessageConstraints;
import org.apache.http.impl.io.HttpTransportMetricsImpl;
import org.apache.http.impl.io.SessionInputBufferImpl;
import org.apache.http.io.BufferInfo;
import org.apache.http.io.HttpTransportMetrics;
import org.apache.http.io.SessionInputBuffer;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.Args;
import org.apache.http.util.Asserts;
import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.CharArrayBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;



@NotThreadSafe
public class SMSessionInputBufferImpl extends SessionInputBufferImpl {


    public SMSessionInputBufferImpl (
            final HttpTransportMetricsImpl metrics,
            final int buffersize,
            final int minChunkLimit,
            final MessageConstraints constraints,
            final CharsetDecoder chardecoder) {
        super(metrics, buffersize, minChunkLimit, constraints, chardecoder);
    }

    public SMSessionInputBufferImpl(
            final HttpTransportMetricsImpl metrics,
            final int buffersize) {
        this(metrics, buffersize, buffersize, null, null);
    }


    public int fillBuffer() throws IOException {

        int l = super.fillBuffer();

        long epoch = System.currentTimeMillis();
        LogMsg.debug("SMSocketInputBuffer.fillBuffer", "First byte read at - " + epoch);
        SMMetrics.INSTANCE.setMetric(SMMetrics.FIRST_BYTE_READ_TIME, epoch);

        return l;
    }

}
