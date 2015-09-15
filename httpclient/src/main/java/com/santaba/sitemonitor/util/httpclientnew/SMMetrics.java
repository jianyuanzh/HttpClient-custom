package com.santaba.sitemonitor.util.httpclientnew;

import java.util.HashMap;

/**
 * The trick is we will use a thread local to save the metrics
 * We use SingleClientConnManager, which according to javadoc, should be used in a single thread context
 * so it's safe to use ThreadLocal here
 */
public class SMMetrics extends ThreadLocal {
    private SMMetrics() {
    }

    private HashMap<String, Object> _metrics = null;

    public Object initialValue() {
        return new HashMap<String, Object>();
    }

    // use of returned reference shall be properly protected!!!!
    public HashMap<String, Object> getMetrics() {
        return (HashMap<String, Object>) super.get();
    }

    public void clearMetrics() {
        HashMap<String, Object> metrics = (HashMap<String, Object>) super.get();
        synchronized (metrics) {
            metrics.clear();
        }
    }

    // METRIC
    public Object getMetric(final String id) {
        HashMap<String, Object> metrics = (HashMap<String, Object>) super.get();
        synchronized (metrics) {
            return metrics.get(id);
        }
    }

    public Object removeMetric(final String id) {
        HashMap<String, Object> metrics = (HashMap<String, Object>) super.get();
        synchronized (metrics) {
            return metrics.remove(id);
        }
    }

    /**
     * If the metrics already exists, we will ignore it
     * This is so because we may encounter redirections. Some metrics will not be measured on
     * redirected content. (It means, on redirection, redirection handler shall clear a metric
     * if it should be measured in redirected content)
     */
    public void setMetric(final String id, final Object obj) {
        setMetric(id, obj, false);
    }

    public void setMetric(final String id, final Object obj, boolean override) {
        HashMap<String, Object> metrics = (HashMap<String, Object>) super.get();
        synchronized (metrics) {
            if (override || !metrics.containsKey(id)) {
                // either explicitly request override the property or it doesn't exist
                metrics.put(id, obj);
            }
        }
    }

    // --------------------------------------------------------------------------------
    // 
    public static SMMetrics INSTANCE = new SMMetrics();

    public static final String REDIRECT_URL = "redirectUrl";
    public static final String DNS_RESOLVE_TIME = "dnsResolveTime";
    public static final String CONNECT_TIME = "connectTime";
    public static final String SSL_HANDSHAKE_TIME = "sslHandshakeTime";
    public static final String FIRST_BYTE_READ_TIME = "firstByteReadTime";
    public static final String READ_TIME = "readTime";

    /**
     * first_byte_read_time = (firstHeaderReadTime - lastEntitySentTime ? lastEntitySentTime : lastHeaderSentTime)
     * total read time = lastEntityReadTime ? (lastEntityReadTime - firstHeaderReadTime) : (lastHeaderReadTime - firstHeaderReadTime)
     */
    public static final String LAST_HEADER_SENT_TIME = "lastHeaderSentTime";
    public static final String LAST_ENTITY_SENT_TIME = "lastEntitySentTime";
    public static final String FIRST_HEADER_READ_TIME = "firstHeaderReadTime";
    public static final String LAST_HEADER_READ_TIME = "lastHeaderReadTime";
    public static final String LAST_ENTITY_READ_TIME = "lastEntityReadTime";
    public static final String LAST_RESPONSE_BODY = "lastResponseBody";
    public static final String LAST_RESPONSE_MIME = "lastResponseMime";
    public static final String LAST_RESPONSE_CHARSET = "lastResponseCharset";

    public long getFirstByteReadTime() {
        HashMap<String, Object> metrics = (HashMap<String, Object>) super.get();
        synchronized (metrics) {
            Long startRead = (Long) metrics.get(FIRST_HEADER_READ_TIME);
            if (metrics.containsKey(LAST_ENTITY_SENT_TIME)) {
                return startRead - (Long) metrics.get(LAST_ENTITY_SENT_TIME);
            }
            else {
                return startRead - (Long) metrics.get(LAST_HEADER_SENT_TIME);
            }
        }
    }

    public long getTotalReadTime() {
        HashMap<String, Object> metrics = (HashMap<String, Object>) super.get();
        synchronized (metrics) {
            Long startRead = (Long) metrics.get(FIRST_HEADER_READ_TIME);
            if (metrics.containsKey(LAST_ENTITY_READ_TIME)) {
                return (Long) metrics.get(LAST_ENTITY_READ_TIME) - startRead;
            }
            else {
                return (Long) metrics.get(LAST_HEADER_READ_TIME) - startRead;
            }
        }
    }

    public long getTotalRequestTime(long start) {
        HashMap<String, Object> metrics = (HashMap<String, Object>) super.get();
        synchronized (metrics) {
            if (metrics.containsKey(LAST_ENTITY_READ_TIME)) {
                return (Long) metrics.get(LAST_ENTITY_READ_TIME) - start;
            }
            else {
                return (Long) metrics.get(LAST_HEADER_READ_TIME) - start;
            }
        }
    }
};
