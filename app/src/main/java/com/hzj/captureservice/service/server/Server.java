package com.hzj.captureservice.service.server;

/**
 * <p>The control of the server.</p>
 * Created by Yan Zhenjie on 2016/6/13.
 */
public interface Server {

    /**
     * Start server.
     */
    void start();

    /**
     * Stop server.
     */
    void stop();

    /**
     * Is the server running?
     *
     * @return return true, not return false.
     */
    boolean isRunning();

    interface Listener {

        /**
         * The server is started.
         */
        void onStarted();

        /**
         * The server is stopped.
         */
        void onStopped();

        /**
         * An error occurred.
         *
         * @param e error.
         */
        void onError(Exception e);

    }
}
