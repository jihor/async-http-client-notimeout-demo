package com.example.demo;

import com.example.demo.server.DemoServer;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.nio.reactor.IOReactorException;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author jihor (jihor@ya.ru)
 * Created on 16.05.2018
 */
@Slf4j
public class DemoClient {

    public static final AtomicInteger counter = new AtomicInteger();

    public static void main(String[] args) throws InterruptedException, IOReactorException {
        startServer();

        TimeUnit.SECONDS.sleep(10); // should be enough for the Spring Boot app to start

        testClients();
        testAsyncClents();
    }

    private static void testClients() {
        Map<String, CloseableHttpClient> clients = new HashMap<>();
        clients.put("Client: default", HttpClientGenerator.defaultClient());
        clients.put("Client: custom RequestConfig", HttpClientGenerator.customClient());

        clients.forEach((k, v) -> {
            invoke(k, v, "Post: no timeout", HttpPostGenerator.post());
            invoke(k, v, "Post: with timeout", HttpPostGenerator.postWithTimeout());
        });
    }

    private static void testAsyncClents() throws IOReactorException {
        Map<String, CloseableHttpAsyncClient> asyncClients = new HashMap<>();
        asyncClients.put("AsyncClient: default", HttpClientGenerator.defaultAsyncClient());
        asyncClients.put("AsyncClient: custom CM, custom ioReactor", HttpClientGenerator.customAsyncClient());

        asyncClients.forEach((k, v) -> {
            invoke(k, v, "Post: no timeout", HttpPostGenerator.post());
            invoke(k, v, "Post: with timeout", HttpPostGenerator.postWithTimeout());
        });
    }

    private static void startServer() {
        Thread server = new Thread(() -> DemoServer.main(new String[]{}));
        server.start();
    }

    private static void invoke(String clientName, CloseableHttpClient httpClient, String postName, HttpPost post) {
        int i = counter.incrementAndGet();
        log.info("Started " + i + " requests");
        String name = postName + " | " + clientName ;
        new Thread(() -> {
            long start = System.currentTimeMillis();
            try {
                CloseableHttpResponse response = httpClient.execute(post);
                print(name, "completed", start, response);
            } catch (IOException e) {
                print(name, "failed", start, e);
            } finally {
                maybeExit();
            }
        }).start();
    }

    private static void invoke(String clientName, CloseableHttpAsyncClient httpAsyncClient, String postName, HttpPost post) {
        int i = counter.incrementAndGet();
        log.info("Started " + i + " requests");
        String name = postName + " | " + clientName ;
        new Thread(() -> {
            httpAsyncClient.start();
            httpAsyncClient.execute(post, callback(name));
        }).start();
    }

    private static void print(String name, String status, long start, Object... details) {
        StringBuilder sb = new StringBuilder();
        sb.append(name + " " + status + " after " + (System.currentTimeMillis() - start) + " msec.");
        if (details.length > 0) {
            sb.append("Details: " + Arrays.asList(details));
        }
        log.info(sb.toString());
    }

    private static void maybeExit() {
        int i = counter.decrementAndGet();
        if (i <= 0) {
            log.info("All requests completed, exiting");
            System.exit(0);
        }
        log.info(i + " requests remaining");
    }

    private static FutureCallback<HttpResponse> callback(String name) {
        return new FutureCallback<HttpResponse>() {

            long start = System.currentTimeMillis();

            @Override
            public void completed(HttpResponse result) {
                print(name, "completed", start, result);
                maybeExit();
            }

            @Override
            public void failed(Exception ex) {
                print(name, "failed", start, ex);
                maybeExit();
            }

            @Override
            public void cancelled() {
                print(name, "cancelled", start);
                maybeExit();
            }
        };
    }
}