package com.hzj.captureservice.util;

/**
 * Created by Hzj
 */
public class ThreadDelay {

    public static void delay(long milis){
        try
        {
            Thread.sleep(milis);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
