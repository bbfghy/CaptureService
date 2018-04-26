package com.hzj.captureservice.service.handler;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;

/**
 * <p>Dealing with the client's request.</p>
 * Created by Yan Zhenjie on 2016/6/13.
 */
public interface RequestHandler {

    /**
     * When is the client request is triggered.
     *
     * @param request  {@link HttpRequest}.
     * @param response {@link HttpResponse}.
     * @param context  {@link HttpContext}.
     * @throws HttpException may be.
     * @throws IOException   read data.
     */
    void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException;
}