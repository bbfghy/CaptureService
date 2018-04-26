package com.hzj.captureservice.service.server;


import com.hzj.captureservice.service.handler.RequestHandler;
import com.hzj.captureservice.service.thread.CoreThread;
import com.hzj.captureservice.service.website.WebSite;

import java.util.Map;

/**
 * <p>Server CoreThread. Mainly is to establish the service side, distribute the requests.</p>
 * Created by Yan Zhenjie on 2016/6/13.
 */
public class DefaultServer implements Server {

    /**
     * Socket mPort.
     */
    private final int mPort;

    /**
     * Timeout.
     */
    private final int mTimeout;

    /**
     * Intercept list.
     */
    private final Map<String, RequestHandler> mHandlerMap;

    /**
     * Website.
     */
    private final WebSite mWebSite;

    /**
     * Server listener.
     */
    private Listener mListener;

    /**
     * Core Thread.
     */
    private CoreThread mCore;

    DefaultServer(int port, int timeout, Map<String, RequestHandler> handlerMap, WebSite webSite, Listener listener)
    {
        this.mPort = port;
        this.mTimeout = timeout;
        this.mHandlerMap = handlerMap;
        this.mWebSite = webSite;
        this.mListener = listener;
    }

    public void registerRequestHandler(String path, RequestHandler request){
        if (isRunning())
            mCore.registerRequestHandler(path,request);
    }

    @Override
    public void start()
    {
        if (!isRunning())
        {
            mCore = new CoreThread(mPort, mTimeout, mHandlerMap, mWebSite, mListener);
            mCore.start();
        }
    }

    @Override
    public void stop()
    {
        if (isRunning())
            mCore.shutdown();
    }

    @Override
    public boolean isRunning()
    {
        return mCore != null && mCore.isRunning();
    }
}
