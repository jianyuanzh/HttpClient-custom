package com.santaba.sitemonitor.util.httpclient;

import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.ssl.SSLContexts;

/**
 * Created by vincent on 9/16/15.
 */
public class SMRegistryFactory {
    public static Registry<ConnectionSocketFactory> createDefault() {
        return RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", SMPlainConnectionSocketFactory.getSocketFactory())
                .register("https", new SMSSLConnectionSocketFactory(SSLContexts.createDefault()))
                .build();
    }
}
