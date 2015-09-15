package com.sm.httpclient;

import com.santaba.sitemonitor.util.httpclient.SMMetrics;
import com.santaba.sitemonitor.util.httpclient.SMSSLConnectionSocketFactory;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.HttpResponse;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import javax.net.ssl.HostnameVerifier;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Created by vincent on 9/15/15.
 */
public class TestSNIWebs {

    /**
     * SNI webs:
     * 1. https://chrismeller.com
     * 2. https://www.five9.com (in production server)
     * 3. https://amicreds.sophosupd.com
     * 4. https://iland.pandell.com
     */
    private String[] sniWebs = new String[]{
            "https://chrismeller.com",
//            "https://www.five9.com",
//            "https://amicreds.sophosupd.com",  // this guy sometimes do not work
//            "https://iland.pandell.com"
    };

    /**
     * With new API for httpClient (use default CloseableHttpClient)
     */
    @Test
    public void test1() {
        CloseableHttpClient closeableHttpClient = HttpClients.createDefault();


        for (String web : sniWebs) {

            HttpGet httpGet = new HttpGet(web);
            HttpResponse response = null;


            try {
                response = closeableHttpClient.execute(httpGet);
                HttpEntity entity = response.getEntity();

                System.out.println("status: " + response.getStatusLine().getStatusCode() + " for web: " + web);
                if (entity != null) {
                    System.out.println("response content:" + EntityUtils.toString(entity).length());
                }
            }
            catch (Exception e) {
                System.out.println("for web : " + web + "  - exception got: " + e.getMessage());
            }

            System.out.println("SMMetics: " + SMMetrics.INSTANCE.getMetrics());

        }
    }

    @Test  //-Djavax.net.debug=ssl
    public void test2() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
        HostnameVerifier hostnameVerifier = SSLConnectionSocketFactory.getDefaultHostnameVerifier();

        sslContextBuilder.loadTrustMaterial(new TrustStrategy() {
            public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                return true;
            }
        });

        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContextBuilder.build(), hostnameVerifier);

        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", sslsf)
                .build();

        CloseableHttpClient closeableHttpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

        for (String web : sniWebs) {

            HttpGet httpGet = new HttpGet(web);
            HttpResponse response = null;
            try {
                response = closeableHttpClient.execute(httpGet);
                HttpEntity entity = response.getEntity();

                System.out.println("status: " + response.getStatusLine().getStatusCode() + " for web: " + web);
                if (entity != null) {
                    System.out.println("response content:" + EntityUtils.toString(entity).length());
                }
            }
            catch (IOException e) {
                System.out.println("exception for web: " + web + ", exception message: " + e.getMessage());
            }

            System.out.println(SMMetrics.INSTANCE.getMetrics());


        }
    }

    @Test
    public void testSmWay() {

    }

}
