package com.hzj.captureservice.service.website;



import com.hzj.captureservice.service.handler.RequestHandler;

import java.util.Map;

/**
 * <p>Registration website interface.</p>
 * Created by Yan Zhenjie on 2017/3/15.
 */
public interface WebSite {

    /**
     * Register site resources.
     *
     * @param handlerMap store handler map.
     */
    void onRegister(Map<String, RequestHandler> handlerMap);

}
