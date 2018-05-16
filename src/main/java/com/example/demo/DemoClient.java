package com.example.demo;

import com.example.demo.server.DemoServer;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.reactor.IOReactorException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author jihor (jihor@ya.ru)
 * Created on 16.05.2018
 */
public class DemoClient {

    public static final AtomicInteger counter = new AtomicInteger();

    public static void main(String[] args) throws InterruptedException, IOReactorException {
        startServer();

        TimeUnit.SECONDS.sleep(10); // should be enough for the Spring Boot app to start

        Map<String, CloseableHttpAsyncClient> clients = new HashMap<>();
        clients.put("Client: default", defaultClient());
        clients.put("Client: customCM, default ioReactor", customCM_defaultReactorClient());
        clients.put("Client: customCM, custom ioReactor", customCM_customReactorClient());

        clients.forEach((k, v) -> {
            invoke(k, v, "Post: no timeout", post());
            invoke(k, v, "Post: with timeout", postWithTimeout());
        });
    }

    private static CloseableHttpAsyncClient defaultClient() {
        return HttpAsyncClients.createDefault();
    }

    private static CloseableHttpAsyncClient customCM_defaultReactorClient() throws IOReactorException {
        DefaultConnectingIOReactor ioReactor = new DefaultConnectingIOReactor();
        PoolingNHttpClientConnectionManager connectionManager = new PoolingNHttpClientConnectionManager(ioReactor);

        return HttpAsyncClients.custom()
                               .setConnectionManager(connectionManager)
                               .build();
    }

    private static CloseableHttpAsyncClient customCM_customReactorClient() throws IOReactorException {
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

    private static void startServer() {
        Thread server = new Thread(() -> DemoServer.main(new String[]{}));
        server.start();
    }

    private static void invoke(String clientName, CloseableHttpAsyncClient httpAsyncClient, String postName, HttpPost post) {
        int i = counter.incrementAndGet();
        System.out.println("Started " + i + " requests");
        String name = clientName + " | " + postName;
        new Thread(() -> {
            httpAsyncClient.start();
            httpAsyncClient.execute(post, callback(name));
        }).start();
    }

    private static HttpPost post() {
        return new HttpPost("http://localhost:8080/test/");
    }

    private static HttpPost postWithTimeout() {
        RequestConfig config = RequestConfig.custom()
                                            .setConnectTimeout(100)
                                            .setSocketTimeout(100)
                                            .setConnectionRequestTimeout(1000)
                                            .build();
        HttpPost request = post();
        request.setConfig(config);
        return request;
    }

    private static FutureCallback<HttpResponse> callback(String name) {
        return new FutureCallback<HttpResponse>() {

            long start = System.currentTimeMillis();

            private void print(String status, long start, Object... details) {
                System.out.println(name + " " + status + " after "
                                           + (System.currentTimeMillis() - start)
                                           + " msec");
                if (details.length > 0) {
                    System.out.println("Details: " + Arrays.asList(details));
                }
                System.out.println();
            }

            private void maybeExit() {
                int i = counter.decrementAndGet();
                if (i <= 0) {
                    System.out.println("All requests completed, exiting");
                    System.exit(0);
                }
                System.out.println(i + " requests remaining");
            }

            @Override
            public void completed(HttpResponse result) {
                print("completed", start, result);
                maybeExit();
            }

            @Override
            public void failed(Exception ex) {
                print("failed", start, ex);
                maybeExit();
            }

            @Override
            public void cancelled() {
                print("cancelled", start);
                maybeExit();
            }
        };
    }
}