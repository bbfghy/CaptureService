package com.hzj.captureservice.service.handler;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import java.io.IOException;

/**
 * Created by Yan Zhenjie on 2017/3/15.
 */
public class DefaultHttpRequestHandler implements HttpRequestHandler {

    private RequestHandler mRequestHandler;

    public DefaultHttpRequestHandler(RequestHandler requestHandler) {
        this.mRequestHandler = requestHandler;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        // Cross domain.
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Server", "AndServer");
        this.mRequestHandler.handle(request, response, context);
    }
}