package com.example.demo;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.reactor.IOReactorException;

/**
 * @author jihor (jihor@ya.ru)
 * Created on 16.05.2018
 */
public class HttpClientGenerator {
    public static CloseableHttpClient defaultClient() {
        return HttpClients.createDefault();
    }

    public static CloseableHttpClient customClient() {
        RequestConfig requestConfig = RequestConfig.custom()
                                                   .setConnectTimeout(100)
                                                   .setSocketTimeout(100)
                                                   .setConnectionRequestTimeout(1000)
                                                   .build();
        return HttpClients.custom()
                          .setDefaultRequestConfig(requestConfig)
                          .build();
    }

    public static CloseableHttpAsyncClient defaultAsyncClient() {
        return HttpAsyncClients.createDefault();
    }

    public static CloseableHttpAsyncClient customAsyncClient() throws IOReactorException {
        IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                                                         .setSelectInterval(100)
                                                         .setConnectTimeout(1000)
                                                         .setSoTimeout(5000)
                                                         .build();
        DefaultConnectingIOReactor ioReactor = new DefaultConnectingIOReactor(ioReactorConfig);
        PoolingNHttpClientConnectionManager connectionManager = new PoolingNHttpClientConnectionManager(ioReactor);
        return HttpAsyncClients.custom()
                               .setConnectionManager(connectionManager)
                               .build();
    }
}
