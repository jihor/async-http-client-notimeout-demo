package com.example.demo;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;

/**
 * @author jihor (jihor@ya.ru)
 * Created on 16.05.2018
 */
public class HttpPostGenerator {
    public static HttpPost post() {
        return new HttpPost("http://localhost:8080/test/");
    }

    public static HttpPost postWithTimeout() {
        RequestConfig config = RequestConfig.custom()
                                            .setConnectTimeout(100)
                                            .setSocketTimeout(3000)
                                            .setConnectionRequestTimeout(1000)
                                            .build();
        HttpPost request = post();
        request.setConfig(config);
        return request;
    }
}
