package com.hzj.captureservice.service.handler;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;

import java.io.IOException;

/**
 * <p>Basic RequestHandler.</p>
 * Created by Yan Zhenjie on 2017/3/16.
 */
public abstract class BasicRequestHandler implements RequestHandler {

    /**
     * Send a 404 response.
     *
     * @param response {@link HttpResponse}.
     * @throws HttpException {@link HttpException}.
     * @throws IOException   {@link IOException}.
     */
    protected void requestInvalid(HttpResponse response) throws HttpException, IOException {
        requestInvalid(response, "The requested resource does not exist.");
    }

    /**
     * Send a 404 response.
     *
     * @param response {@link HttpResponse}.
     * @param message  message.
     * @throws HttpException {@link HttpException}.
     * @throws IOException   {@link IOException}.
     */
    protected void requestInvalid(HttpResponse response, String message) throws HttpException, IOException {
        response.setStatusCode(404);
        response.setEntity(new StringEntity(message));
    }

}
