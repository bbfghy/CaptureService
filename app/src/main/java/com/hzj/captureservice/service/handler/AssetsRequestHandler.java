package com.hzj.captureservice.service.handler;

import com.hzj.captureservice.util.AssetsWrapper;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.io.InputStream;

/**
 * <p>Asset file handler.</p>
 * Created by Yan Zhenjie on 2017/3/15.
 */

public class AssetsRequestHandler extends BasicRequestHandler {

    /**
     * Asset handler wrapper.
     */
    private AssetsWrapper mAssetsWrapper;

    /**
     * Target file path.
     */
    private String mFilePath;

    /**
     * Create a handler for file.
     *
     * @param assetsWrapper Asset handler wrapper.
     * @param mFilePath     absolute file path.
     */
    public AssetsRequestHandler(AssetsWrapper assetsWrapper, String mFilePath) {
        this.mAssetsWrapper = assetsWrapper;
        this.mFilePath = mFilePath;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        InputStream stream = mAssetsWrapper.getInputStream(mFilePath);
        if (stream == null) {
            requestInvalid(response);
        } else {
            response.setStatusCode(200);
            response.setEntity(new InputStreamEntity(stream, stream.available()));
        }
    }

}
