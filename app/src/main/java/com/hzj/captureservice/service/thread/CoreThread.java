package com.hzj.captureservice.service.thread;

import com.hzj.captureservice.service.handler.DefaultHttpRequestHandler;
import com.hzj.captureservice.service.handler.RequestHandler;
import com.hzj.captureservice.service.server.Server;
import com.hzj.captureservice.util.Executors;
import com.hzj.captureservice.service.website.BasicWebsite;
import com.hzj.captureservice.service.website.WebSite;

import org.apache.http.client.protocol.ResponseProcessCookies;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.protocol.HttpRequestHandlerResolver;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Yan Zhenjie on 2017/3/13.
 */
public class CoreThread extends Thread {

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
     * WebSite.
     */
    private final WebSite mWebSite;

    /**
     * Server listener.
     */
    private Server.Listener mListener;

    /**
     * Loop.
     */
    private boolean isLoop;

    /**
     * Server socket.
     */
    private ServerSocket mServerSocket;

    private SoftReference<HttpRequestHandlerRegistry> hrCache;

    /**
     * To construct the service.
     *
     * @param port       mPort code.
     * @param timeout    mTimeout.
     * @param handlerMap api list.
     * @param listener   listener.
     */
    public CoreThread(int port, int timeout, Map<String, RequestHandler> handlerMap, WebSite webSite, Server.Listener listener)
    {
        this.mPort = port;
        this.mTimeout = timeout;
        this.mHandlerMap = handlerMap;
        this.mWebSite = webSite;
        this.mListener = listener;
    }

    public void registerRequestHandler(String path, RequestHandler request)
    {
        if (hrCache!=null&&hrCache.get()!=null)
        {
            hrCache.get().register(BasicWebsite.getHttpPath(path),
                    new DefaultHttpRequestHandler(request));
        }
    }


    /**
     * Create HttpParams.
     *
     * @return {@link HttpParams}.
     */
    private HttpParams createHttpParams()
    {
        return new BasicHttpParams()
                .setIntParameter(CoreConnectionPNames.SO_TIMEOUT, mTimeout)
                .setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024)
                .setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false)
                .setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
                .setParameter(CoreProtocolPNames.ORIGIN_SERVER, "WebServer/1.1");
    }

    /**
     * Create HttpProcessor.
     *
     * @return {@link HttpProcessor}.
     */
    private HttpProcessor createHttpProcessor()
    {
        BasicHttpProcessor httpProcessor = new BasicHttpProcessor();
        httpProcessor.addInterceptor(new ResponseContent());
        httpProcessor.addInterceptor(new ResponseConnControl());
        httpProcessor.addInterceptor(new ResponseDate());
        httpProcessor.addInterceptor(new ResponseServer());
        httpProcessor.addInterceptor(new ResponseProcessCookies());
        return httpProcessor;
    }

    /**
     * Register handler.
     *
     * @return {@link HttpRequestHandlerResolver}.
     */
    private HttpRequestHandlerResolver registerRequestHandler()
    {
        HttpRequestHandlerRegistry handlerRegistry = new HttpRequestHandlerRegistry();

        hrCache=new SoftReference<HttpRequestHandlerRegistry>(handlerRegistry);

        for (Map.Entry<String, RequestHandler> handler : mHandlerMap.entrySet())
        {
            handlerRegistry.register(BasicWebsite.getHttpPath(handler.getKey()),
                    new DefaultHttpRequestHandler(handler.getValue()));
        }

        Map<String, RequestHandler> websiteMap = new HashMap<>(3);
        if (mWebSite!=null)
            mWebSite.onRegister(websiteMap);
        if (websiteMap.size() > 0)
        {
            for (Map.Entry<String, RequestHandler> handler : websiteMap.entrySet())
            {
                handlerRegistry.register(BasicWebsite.getHttpPath(handler.getKey()),
                        new DefaultHttpRequestHandler(handler.getValue()));
            }
        }
        return handlerRegistry;
    }

    /**
     * Create Http service.
     *
     * @param httpParams      {@link HttpParams}.
     * @param httpProcessor   {@link HttpProcessor}.
     * @param handlerRegistry {@link HttpRequestHandlerResolver}.
     * @return {@link HttpService}.
     */
    private HttpService createHttpService(HttpParams httpParams, HttpProcessor httpProcessor, HttpRequestHandlerResolver
            handlerRegistry)
    {
        HttpService httpService = new HttpService(httpProcessor, new DefaultConnectionReuseStrategy(),
                new DefaultHttpResponseFactory());
        httpService.setParams(httpParams);
        httpService.setHandlerResolver(handlerRegistry);
        return httpService;
    }

    @Override
    public void run()
    {
        // HTTP Attribute.
        HttpParams httpParams = createHttpParams();

        // Protocol intercept.
        HttpProcessor httpProcessor = createHttpProcessor();

        // Register handler.
        HttpRequestHandlerResolver handlerRegistry = registerRequestHandler();

        // Create server.
        HttpService httpService = createHttpService(httpParams, httpProcessor, handlerRegistry);

        try
        {
            mServerSocket = new ServerSocket();
            mServerSocket.setReuseAddress(true);
            try
            {
                mServerSocket.bind(new InetSocketAddress(mPort));
            }
            catch (final IOException ignored)
            {
                if (mListener != null)
                    Executors.getInstance().handler(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            mListener.onError(ignored);
                        }
                    });
                return;
            }

            if (mListener != null)
                Executors.getInstance().handler(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        mListener.onStarted();
                    }
                });

            isLoop = true;

            while (isLoop)
            {
                if (!mServerSocket.isClosed())
                {
                    Socket socket = mServerSocket.accept();
                    DefaultHttpServerConnection serverConnection = new DefaultHttpServerConnection();
                    serverConnection.bind(socket, httpParams);

                    // Dispatch request handler.
                    HandleRequestThread requestTask = new HandleRequestThread(this, httpService, serverConnection);
                    requestTask.setDaemon(true);
                    Executors.getInstance().executorService(requestTask);
                }
            }
        } catch (final Exception ignored)
        {
            ignored.printStackTrace();
        }
        finally
        {
            shutdown();
        }
    }

    /**
     * Stop core server.
     */
    public void shutdown()
    {
        isLoop = false;
        try
        {
            if (mServerSocket != null)
                mServerSocket.close();
        }
        catch (IOException ignored)
        {
        }
        if (isInterrupted())
            interrupt();
        if (mListener != null)
            Executors.getInstance().handler(new Runnable()
            {
                @Override
                public void run()
                {
                    mListener.onStopped();
                }
            });
    }

    /**
     * Is the server running?
     *
     * @return true, other wise is false.
     */
    public boolean isRunning()
    {
        return isLoop;
    }
}