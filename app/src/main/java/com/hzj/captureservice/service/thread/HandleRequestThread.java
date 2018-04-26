package com.hzj.captureservice.service.thread;

import org.apache.http.HttpServerConnection;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpService;

import java.io.IOException;

/**
 * Created by Yan Zhenjie on 2016/6/13.
 */
public class HandleRequestThread extends Thread {

    private final HttpService mHttpService;

    private final HttpServerConnection mConnection;

    private CoreThread mCore;

    public HandleRequestThread(CoreThread core, HttpService httpservice, HttpServerConnection connection) {
        this.mCore = core;
        this.mHttpService = httpservice;
        this.mConnection = connection;
    }

    @Override
    public void run() {
        try {
            while (mCore.isRunning() && mConnection.isOpen()) {
                mHttpService.handleRequest(mConnection, new BasicHttpContext());
            }
        } catch (Exception ignored) {
        } finally {
            try {
                mConnection.shutdown();
            } catch (IOException ignored) {
            }
        }
    }
}
