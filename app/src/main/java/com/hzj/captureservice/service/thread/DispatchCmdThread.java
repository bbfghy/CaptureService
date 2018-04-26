package com.hzj.captureservice.service.thread;

import android.os.Handler;
import android.util.Log;

import com.hzj.captureservice.bean.CmdInfo;
import com.hzj.captureservice.queue.ObjectManager;
import com.hzj.captureservice.util.Command;
import com.hzj.captureservice.util.Constant;
import com.hzj.captureservice.util.ThreadDelay;
import com.hzj.captureservice.util.ThreadLock;
import com.hzj.captureservice.util.LogUtil;

/**
 * 分发指令线程(出队列)
 * Created by Hzj
 */
public class DispatchCmdThread extends Thread {

    private boolean isRunning=true;
    private String TAG=DispatchCmdThread.class.getSimpleName();
    private Handler mhandle;

    public DispatchCmdThread(Handler mhandle) {
        this.mhandle = mhandle;
    }

    public void run()
    {
        CmdInfo cmd = null;
        while (isRunning)//DispatchCmdThread
        {
            try
            {
                cmd = ObjectManager.allocateSnapshotCmdInfo();
            }
            catch (Exception e)
            {
                e.printStackTrace();
                cmd = null;
                continue;
            }

            if (cmd==null){
                ThreadDelay.delay(100);
                continue;
            }

            switch (cmd.cmdId)
            {
                case Command.CMD_RECEIVE_SNAPSHOT_ID:
                    LogUtil.d(TAG,"cmdId="+cmd.cmdId);
                    mhandle.obtainMessage(Constant.HANDLER_SNAPSHOT_CMD,cmd).sendToTarget();
                    ThreadLock.setIsWaiting(true);
                    ThreadLock.threadWaitLock();
                    LogUtil.d(TAG,"DispatchCmdThread Constant.CMD_SNAPSHOT notify");
                    break;
                default:
                    Log.e(TAG, "DispatchCmdThread() info: sorry, I forget send cmd=" + cmd);
                    LogUtil.CAPTURE_WRITE_LOG(LogUtil.LOG_LEVEL_ERROR, TAG + "," + "DispatchCmdThread() info: sorry, I forget send cmd=" + cmd);
                    cmd = null;
                    break;
            }
            ThreadDelay.delay(100);
        }
    }

    public void setRunning(boolean running)
    {
        this.isRunning = running;
    }

}
