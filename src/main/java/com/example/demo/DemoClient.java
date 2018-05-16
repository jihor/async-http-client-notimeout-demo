package com.example.demo;

import com.example.demo.server.DemoServer;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.nio.reactor.IOReactorException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author jihor (jihor@ya.ru)
 * Created on 16.05.2018
 */
public class DemoClient {

    public static final AtomicInteger counter = new AtomicInteger(3);

    public static void main(String[] args) throws InterruptedException, IOReactorException {
        startServer();

        TimeUnit.SECONDS.sleep(10); // should be enough for the Spring Boot app to start

        startClient1();
        startClient2();
        startClient3();
    }

    private static void startClient1() {
        String name = "Default_config";
        CloseableHttpAsyncClient httpAsyncClient = HttpAsyncClients.createDefault();
        invoke(httpAsyncClient, name);
    }

    private static void startClient2() throws IOReactorException {
        String name = "Config_with_custom_connection_manager_and_default_ioReactor";

        DefaultConnectingIOReactor ioReactor = new DefaultConnectingIOReactor();
        PoolingNHttpClientConnectionManager connectionManager = new PoolingNHttpClientConnectionManager(ioReactor);

        CloseableHttpAsyncClient httpAsyncClient = HttpAsyncClients.custom()
                                                                   .setConnectionManager(connectionManager)
                                                                   .build();

        invoke(httpAsyncClient, name);
    }

    private static void startClient3() throws IOReactorException {
        String name = "Config_with_custom_connection_manager_and_custom_ioReactor";

        IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                                                   .setSelectInterval(100)
                                                   .setConnectTimeout(1000)
                                                   .setSoTimeout(5000)
                                                   .build();
        DefaultConnectingIOReactor ioReactor = new DefaultConnectingIOReactor(ioReactorConfig);
        PoolingNHttpClientConnectionManager connectionManager = new PoolingNHttpClientConnectionManager(ioReactor);
        CloseableHttpAsyncClient httpAsyncClient = HttpAsyncClients.custom()
                                                                   .setConnectionManager(connectionManager)
                                                                   .build();

        invoke(httpAsyncClient, name);
    }

    private static void startServer() {
        Thread server = new Thread(() -> DemoServer.main(new String[]{}));
        server.start();
    }

    private static void invoke(CloseableHttpAsyncClient httpAsyncClient, String name){
        new Thread(() -> {
            httpAsyncClient.start();
            httpAsyncClient.execute(post(name), callback(name));
        }).start();
    }

    private static HttpPost post(String name){
        HttpPost request = new HttpPost("http://localhost:8080/test/");
        URIBuilder builder = new URIBuilder(request.getURI());
        builder.addParameter("name", name);
        try {
            URI uri = builder.build();
            request.setURI(uri);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
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
            }

            private void maybeExit() {
                if (counter.decrementAndGet() <= 0) {
                    System.exit(0);
                }
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