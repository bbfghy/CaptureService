
package com.hzj.captureservice.queue;

import android.util.Log;


import com.hzj.captureservice.bean.CmdInfo;
import com.hzj.captureservice.bean.CmdSaveInfo;
import com.hzj.captureservice.bean.LogDataInfo;
import com.hzj.captureservice.bean.SendDataInfo;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by hzj
 */
public class ObjectManager {

    public static final String TAG = "ObjectManager";

    public static final int CMDINFO_QUEUE_MAX = 1024;
    public static final int MAX_COUNT_LOG_DATA_INFO = 1064;

    /**截图指令队列*/
    public static BlockingQueue<CmdInfo> cmdInfo_queue = new ArrayBlockingQueue<>(CMDINFO_QUEUE_MAX);
    public static BlockingQueue<CmdInfo> snapshot_queue = new ArrayBlockingQueue<>(CMDINFO_QUEUE_MAX);
    /**发送指令队列*/
    public static BlockingQueue<SendDataInfo> sendInfo_queue = new ArrayBlockingQueue<>(CMDINFO_QUEUE_MAX);
    public static BlockingQueue<SendDataInfo> sendCmdInfo_queue = new ArrayBlockingQueue<>(CMDINFO_QUEUE_MAX);
    /**压缩保存图片队列*/
    public static BlockingQueue<CmdSaveInfo> cmdSaveInfo_queue = new ArrayBlockingQueue<>(CMDINFO_QUEUE_MAX);
    /**日志*/
    public static BlockingQueue<LogDataInfo> logDataInfoQueue = new ArrayBlockingQueue<>(MAX_COUNT_LOG_DATA_INFO);

    public static int cmdInfo_allocated_count = 0;
    public static int sendcmdInfo_allocated_count = 0;
    public static int log_data_info_allocated_count = 0;

    private static Lock sendInfoLock = new ReentrantLock();
    private static Lock sendCmdLock = new ReentrantLock();

    private static Lock cmdSnapInfoLock = new ReentrantLock();
    private static Lock cmdInfoLock = new ReentrantLock();

    private static Lock cmdSaveInfoLock = new ReentrantLock();
    private static Lock LogDataInfoObj_lock = new ReentrantLock();

    private static int allocateTime = 0;
    private static int freeTime = 0;
    private static final int maxTime = 10000;


    public static SendDataInfo allocateSendCmdInfo(){

        SendDataInfo obj=null;
        sendCmdLock.lock();
        try
        {
            obj = sendCmdInfo_queue.take();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        sendCmdLock.unlock();

        return obj;
    }

    public static SendDataInfo allocateSendInfoObj()
    {

        SendDataInfo obj;
        sendInfoLock.lock();

        obj = sendInfo_queue.poll();
        if (obj == null)
        {
            if (sendcmdInfo_allocated_count < CMDINFO_QUEUE_MAX)
            {
                obj = new SendDataInfo();
                sendcmdInfo_allocated_count++;
                obj.obj_id = sendcmdInfo_allocated_count;
            }
            else
            {
                Log.e(TAG, "allocateCmdInfoObj failed because cmdInfo_allocated_count > CMDINFO_QUEUE_MAX =" + CMDINFO_QUEUE_MAX);
            }
        }

        obj.init();

        sendInfoLock.unlock();

        return obj;
    }


    public static CmdInfo allocateSnapshotCmdInfo(){

        CmdInfo obj=null;
        cmdSnapInfoLock.lock();
        try
        {
            obj = snapshot_queue.take();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        cmdSnapInfoLock.unlock();

        return obj;
    }


    public static LogDataInfo allocateLogDataInfoObj()
    {
        LogDataInfo obj;
        LogDataInfoObj_lock.lock();

        obj = logDataInfoQueue.poll();
        if (obj == null)
        {
            if (log_data_info_allocated_count < MAX_COUNT_LOG_DATA_INFO)
            {
                obj = new LogDataInfo();

                log_data_info_allocated_count++;
            }
        }

        obj.init();

        LogDataInfoObj_lock.unlock();
        return obj;
    }


    public static CmdInfo allocateCmdInfoObj()
    {

        CmdInfo obj;
        cmdInfoLock.lock();

        obj = cmdInfo_queue.poll();
        if (obj == null)
        {
            if (cmdInfo_allocated_count < CMDINFO_QUEUE_MAX)
            {
                obj = new CmdInfo();
                cmdInfo_allocated_count++;
                obj.obj_id = cmdInfo_allocated_count;
            }
            else
            {
                Log.e(TAG, "allocateCmdInfoObj failed because cmdInfo_allocated_count > CMDINFO_QUEUE_MAX =" + CMDINFO_QUEUE_MAX);
            }
        }

        obj.init();

        allocateTime++;

        cmdInfoLock.unlock();

        if (allocateTime > maxTime)
        {
            allocateTime = 0;
        }

        return obj;
    }


    public static CmdSaveInfo allocateSaveCmdInfoObj()
    {

        CmdSaveInfo obj=null;
        cmdSaveInfoLock.lock();

        try
        {
            obj = cmdSaveInfo_queue.take();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        if (obj == null)
        {
            if (cmdInfo_allocated_count < CMDINFO_QUEUE_MAX)
            {
                obj = new CmdSaveInfo();
                cmdInfo_allocated_count++;
            }
            else
            {
                Log.e(TAG, "allocateCmdInfoObj failed because cmdInfo_allocated_count > CMDINFO_QUEUE_MAX =" + CMDINFO_QUEUE_MAX);
            }
        }
        cmdSaveInfoLock.unlock();
        return obj;
    }

    public static void freeCmdInfoQueueObj(CmdInfo obj)
    {
        cmdInfoLock.lock();
        if (obj != null && !cmdInfo_queue.offer(obj))
        {
            Log.e(TAG, "freeCmdInfoQueueObj error");
        }
        freeTime++;
        cmdInfoLock.unlock();

        if (freeTime > maxTime)
        {
            freeTime = 0;
        }
    }

    public static void freeLogDataInfoQueueObj(LogDataInfo obj)
    {
        LogDataInfoObj_lock.lock();

        if (!logDataInfoQueue.offer(obj))
        {
            Log.e(TAG, "freeLogDataInfoQueueObj error");
        }

        LogDataInfoObj_lock.unlock();
    }

    public static void freeSendInfoQueueObj(SendDataInfo cmd)
    {
        sendInfoLock.lock();

        if (!sendInfo_queue.offer(cmd))
        {
            Log.e(TAG, "freeSendInfoQueueObj error");
        }

        sendInfoLock.unlock();
    }
}
