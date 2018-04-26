package com.hzj.captureservice.util;

/**
 * Created by Hzj
 */
public class ThreadLock {

    private static Object mThreadLock=new Object();

    private static boolean isWaiting=false;

    public static void threadWaitLock(){
        if (isWaiting)
        {
            isWaiting=false;
            try
            {
                synchronized (mThreadLock)
                {
                    mThreadLock.wait();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void setIsWaiting(boolean isWaiting)
    {
        ThreadLock.isWaiting=isWaiting;
    }

    public static void notifyLock(){
        try
        {
            synchronized (mThreadLock)
            {
                mThreadLock.notifyAll();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
