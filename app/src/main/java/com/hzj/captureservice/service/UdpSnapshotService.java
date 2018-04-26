package com.hzj.captureservice.service;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.os.AsyncTaskCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;


import com.hzj.captureservice.bean.CmdInfo;
import com.hzj.captureservice.bean.CmdSaveInfo;
import com.hzj.captureservice.bean.SendDataInfo;
import com.hzj.captureservice.queue.ObjectManager;
import com.hzj.captureservice.service.handler.RequestFileHandler;
import com.hzj.captureservice.service.server.AndServer;
import com.hzj.captureservice.service.server.DefaultServer;
import com.hzj.captureservice.service.server.Server;
import com.hzj.captureservice.service.thread.DispatchCmdThread;
import com.hzj.captureservice.service.thread.DispatchSaveCmdThread;
import com.hzj.captureservice.service.thread.UdpCmdSendThread;
import com.hzj.captureservice.service.thread.UdpCmdReceiver;
import com.hzj.captureservice.ui.ScreenCaptureActivity;
import com.hzj.captureservice.util.Command;
import com.hzj.captureservice.util.Constant;
import com.hzj.captureservice.util.FileUtil;
import com.hzj.captureservice.util.ShareUtils;
import com.hzj.captureservice.util.ThreadLock;
import com.hzj.captureservice.util.LogUtil;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;


/**
 * Created by Hzj
 */
public class UdpSnapshotService extends Service {

    private static final String TAG = UdpSnapshotService.class.getSimpleName();

    private long startTime;
    int app_config_log_level = LogUtil.LOG_LEVEL_INFO;
    public LogUtil AppLogObj;

    /**线程*/
    private UdpCmdReceiver mUdpReceiveThread;
    private DispatchCmdThread mDispatchThread;
    private UdpCmdSendThread sendThread;
    private DispatchSaveCmdThread saveDispatchThread;

    /**------------------------截图---------------------------------*/
    private MediaProjectionManager mediaProjectionManager;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;

    private  Intent mProjectionResultData = null;
    private ImageReader mImageReader;
    private int mScreenWidth;
    private int mScreenHeight;
    private int mScreenDensity;

    /**截图广播*/
    private SnapshotBroadcast mBroadcastReceiver;

    /**
     * AndServer.
     */
    private Server mServer;


    @Nullable @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onCreate()
    {
        AppLogObj = new LogUtil(this, app_config_log_level);
        AppLogObj.startThread();

        Constant.SD_PICTURE_COUNT= ShareUtils.getParam(this,Constant.PREKEY_PICTURE_COUNT,0);

        startTime=System.currentTimeMillis();
        //启动线程
        mUdpReceiveThread=new UdpCmdReceiver();
        mUdpReceiveThread.start();

        mDispatchThread=new DispatchCmdThread(mhand);
        mDispatchThread.start();

        saveDispatchThread=new DispatchSaveCmdThread(mhand);
        saveDispatchThread.start();

        sendThread=new UdpCmdSendThread();
        sendThread.start();

        //初始化截图
        initSnapshotFun();
        //注册广播
        mBroadcastReceiver=new SnapshotBroadcast();
        IntentFilter filter=new IntentFilter();
        filter.addAction(Constant.BROADCAST_ACTION_SCREEN_CAPTURE);
        registerReceiver(mBroadcastReceiver,filter);

        //建立web服务器
        AndServer andServer = new AndServer.Build()
                .port(Command.WEB_PORT)
                .timeout(10 * 1000)
                .build();

        // Create server.
        mServer = andServer.createServer();
        startServer();
    }



    /**线程处理消息*/
    Handler mhand=new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case Constant.HANDLER_SNAPSHOT_CMD://开始截图
                    CmdInfo info= (CmdInfo) msg.obj;
                    startScreenShot(info.clientIP);
                    break;
                case Constant.HANDLER_SAVE_SNAPSHOT_MSGID://保存截图后发送链接
                    String arr[]= (String[]) msg.obj;
                    registerRequestHandler(arr[0]);
                    sendCmd(arr[0],arr[1]);
                    break;
            }
        }
    };

    /**------------------------------------web服务---------------------------------------------------------*/
    /**
     * Start server.
     */
    private void startServer()
    {
        if (mServer != null)
        {
            if (mServer.isRunning())
            {
            }
            else
            {
                mServer.start();
            }
        }
    }

    /**
     * 注册一条可从web服务器下载的链接
     * @param path
     */
    private void registerRequestHandler(String path)
    {
        ((DefaultServer)mServer).registerRequestHandler(path, new RequestFileHandler(path));
    }

    /**------------------------------------初始化截图-------------------------------------------------*/
    private void initSnapshotFun()
    {
        // 0) 创建ImageReader
        createImageReader();
        // 1) 获取MediaProjectionManager类实例
        mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        // 2) 由ScreenCaptureActivity createScreenCaptureIntent
        startActivity(new Intent(UdpSnapshotService.this, ScreenCaptureActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    private void createImageReader()
    {
        WindowManager mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        DisplayMetrics metrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;

        mImageReader = ImageReader.newInstance(mScreenWidth, mScreenHeight, PixelFormat.RGBA_8888, 1);
    }

    /**----------------------------------开始截图----------------------------------------------------*/
    class SnapshotBroadcast extends BroadcastReceiver
    {

        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action=intent.getAction();
            if (action!=null&&action.equals(Constant.BROADCAST_ACTION_SCREEN_CAPTURE))
            {
                setProjectionResultData(intent);
            }
        }
    }

    private void setProjectionResultData(Intent resultData)
    {
        // 3) 保存resultData
        mProjectionResultData = resultData;
    }

    private void startScreenShot(String ip)
    {
        int callCode = 0;

        if (mMediaProjection == null)
        {
            callCode = getMediaProjection();
        }
        if (callCode != 0)
        {
            return;
        }

        if (Build.VERSION.SDK_INT >= 21 )
        {
            mVirtualDisplay = mMediaProjection.createVirtualDisplay("screen-mirror",
                    mScreenWidth, mScreenHeight, mScreenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    mImageReader.getSurface(), null, null);
        }

        startCapture(ip);
    }

    public int getMediaProjection()
    {
        int rstCode = 0;
        if (Build.VERSION.SDK_INT < 21)
        {
            rstCode = 1;
            LogUtil.e(TAG, "getMediaProjection failed, Build.VERSION.SDK_INT < 21");
        }
        else if (mProjectionResultData == null)
        {
            rstCode = 2;
            LogUtil.e(TAG, "getMediaProjection failed, mProjectionResultData == null");
        }
        else
        {
            // 4) 通过Activity.RESULT_OK和mProjectionResultData 获取当前的屏幕映射
            mMediaProjection = mediaProjectionManager.getMediaProjection(Activity.RESULT_OK, mProjectionResultData);
        }
        return rstCode;
    }


    private void startCapture(String ip)
    {

        Image image = mImageReader.acquireLatestImage();
        if(image == null)
        {
            startScreenShot(ip);
            return;
        }
        // 6) 转化并保存截取的屏幕数据到文件
        CmdSaveInfo info=new CmdSaveInfo();
        info.setCmdId(Command.CMD_SAVE_SNAPSHOT_ID);
        info.setmImage(image);
        info.setStartTime(startTime);
        info.setSourceIp(ip);
        info.setFileNameNo(Constant.FILE_NAME_NO);
        try
        {
            ObjectManager.cmdSaveInfo_queue.put(info);//放入保存队列
            info=null;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        //sd卡上图片数量检测，当超过30张时，删除最旧的25张
        Constant.SD_PICTURE_COUNT++;
        if (Constant.SD_PICTURE_COUNT<Constant.MAX_PICTURE_COUNT)
            ShareUtils.setParam(this,Constant.PREKEY_PICTURE_COUNT,Constant.SD_PICTURE_COUNT);
        else
        {
            AsyncTask task=new AsyncTask()
            {
                @Override
                protected Object doInBackground(Object[] params)
                {
                    String dir = FileUtil.getSDPATH() + "Snapshot";
                    File fileDic=new File(dir);
                    File[] fileList = fileDic.listFiles();
                    //按修改时间递增排序
                    Arrays.sort(fileList, new Comparator<File>()
                    {
                        public int compare(File f1, File f2)
                        {
                            long diff = f1.lastModified() - f2.lastModified();
                            if (diff > 0)
                                return 1;
                            else if (diff == 0)
                                return 0;
                            else
                                return -1;//如果 if 中修改为 返回-1 同时此处修改为返回 1  排序就会是递减
                        }

                        public boolean equals(Object obj)
                        {
                            return true;
                        }

                    });
                    int count=0;
                    for (File f:fileList)
                    {
                        if (count==Constant.MAX_DELETE_COUNT)break;
                        if (f!=null&&f.exists()&&f.isFile())
                        {
                            f.delete();
                            count++;
                            Constant.SD_PICTURE_COUNT--;
                        }
                    }
                    ShareUtils.setParam(UdpSnapshotService.this,Constant.PREKEY_PICTURE_COUNT,Constant.SD_PICTURE_COUNT);
                    return null;
                }
            };
            AsyncTaskCompat.executeParallel(task);
        }

    }


    /**
     *  发送截图
     * @param path
     */
    private void sendCmd(String path,String ip)
    {
        File file=new File(path);
        if (file.exists())
        {
            sendCmdInQueue(path, ip);

            for (int i=0,size=ObjectManager.snapshot_queue.size();i<size;i++)
            {
                CmdInfo info=ObjectManager.snapshot_queue.poll();
                sendCmdInQueue(path, info.clientIP);
            }
            //通知线程释放同步锁
            ThreadLock.notifyLock();
        }
    }

    private void sendCmdInQueue(String path, String ip) {
        SendDataInfo cmd =null;
        try
        {
            cmd = ObjectManager.allocateSendInfoObj();
            cmd.path=path;
            cmd.ip=ip;
            ObjectManager.sendCmdInfo_queue.put(cmd);

            if (cmd != null)
            {
                ObjectManager.freeSendInfoQueueObj(cmd);// 归还对象池
                cmd = null;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    @Override
    public boolean stopService(Intent name)
    {
        try
        {
            mUdpReceiveThread.setRunning(false);
            mDispatchThread.setRunning(false);
            sendThread.setRunning(false);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return super.stopService(name);
    }


    @Override
    public void onDestroy()
    {
        unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
        freeSnapshotFun();
    }

    /**------------------------------释放截图资源----------------------------------------------------*/
    private void freeSnapshotFun()
    {
        stopVirtual();
        stopMediaProjection();
    }

    private void stopMediaProjection()
    {
        if (Build.VERSION.SDK_INT >= 21)
        {
            if (mMediaProjection != null)
            {
                mMediaProjection.stop();
                mMediaProjection = null;
            }
        }
    }

    private void stopVirtual()
    {
        if (mVirtualDisplay == null)
        {
            return;
        }
        mVirtualDisplay.release();
        mVirtualDisplay = null;
    }

}
