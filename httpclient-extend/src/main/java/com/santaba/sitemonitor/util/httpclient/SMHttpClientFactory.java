package com.santaba.sitemonitor.util.httpclient;

import com.santaba.sitemonitor.util.httpclient.base.SMBasicHttpClientConnectionManager;
import com.santaba.sitemonitor.util.httpclient.base.SMManagedHttpClientConnectionFactory;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * Created by vincent on 9/16/15.
 */
public class SMHttpClientFactory {

    public static final CloseableHttpClient create() {

        CloseableHttpClient closeableHttpClient = HttpClients.custom()
                .setConnectionManager(new SMBasicHttpClientConnectionManager(SMRegistryFactory.createDefault(),
                        new SMManagedHttpClientConnectionFactory()))
                .build();

        return closeableHttpClient;
    }



}
