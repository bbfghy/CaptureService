package com.hzj.captureservice.service.thread;

import android.graphics.Bitmap;
import android.media.Image;
import android.os.Handler;


import com.hzj.captureservice.bean.CmdSaveInfo;
import com.hzj.captureservice.queue.ObjectManager;
import com.hzj.captureservice.util.Command;
import com.hzj.captureservice.util.Constant;
import com.hzj.captureservice.util.FileUtil;
import com.hzj.captureservice.util.ThreadDelay;
import com.hzj.captureservice.util.LogUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by Hzj
 */
public class DispatchSaveCmdThread extends Thread {

    private static final String TAG = "SnapshotService";

    private final Handler mhand;
    private boolean isRunning=true;

    public DispatchSaveCmdThread(Handler mhand) {
        this.mhand=mhand;
    }

    @Override
    public void run() {
        super.run();
        CmdSaveInfo cmd;
        while (isRunning){
            try
            {
                cmd = ObjectManager.allocateSaveCmdInfoObj();
            }
            catch (Exception e)
            {
                e.printStackTrace();
                cmd = null;
            }

            if (cmd==null){
                ThreadDelay.delay(50);
                continue;
            }

            switch (cmd.getCmdId())
            {
                case Command.CMD_SAVE_SNAPSHOT_ID:
                    dealSaveCmd(cmd);
                    break;
                default:
                    cmd = null;
                    break;
            }
            ThreadDelay.delay(50);
        }
    }


    private void dealSaveCmd(CmdSaveInfo cmd) {
        LogUtil.d(TAG, "save image---");
        if (cmd == null)
        {
            return ;
        }

        Image image =cmd.getmImage();
        Bitmap bitmap=cmd.getBitmap();
        if (image!=null){
            int width = image.getWidth();
            int height = image.getHeight();
            final Image.Plane[] planes = image.getPlanes();
            final ByteBuffer buffer = planes[0].getBuffer();
            //每个像素的间距
            int pixelStride = planes[0].getPixelStride();
            //总的间距
            int rowStride = planes[0].getRowStride();
            int rowPadding = rowStride - pixelStride * width;
            bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_4444);
            bitmap.copyPixelsFromBuffer(buffer);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);

            image.close();
        }
        File fileImage = null;
        if (bitmap != null)
        {
            try
            {
                fileImage = createSnapshotFile(cmd.getFileNameNo());
                FileOutputStream out = new FileOutputStream(fileImage);

                if (out != null)
                {
                   bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.flush();
                    out.close();
                }
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
                fileImage = null;
            }
            catch (IOException e)
            {
                e.printStackTrace();
                fileImage = null;
            }
        }

        if (fileImage != null&&fileImage.exists())
        {
            Constant.FILE_NAME_NO++;
             String filePath=fileImage.getAbsolutePath();
            String[] arr=new String[2];
            arr[0]=filePath;
            arr[1]=cmd.getSourceIp();
            mhand.obtainMessage(Constant.HANDLER_SAVE_SNAPSHOT_MSGID,arr).sendToTarget();
            LogUtil.d(TAG,"snapshot capture cost :"+(System.currentTimeMillis()-cmd.getStartTime()));
        }
        cmd.init();
        cmd=null;
    }

    private File createSnapshotFile(int no)
    {
        if (no>Constant.MAX_FILE_NAME_NO){
            Constant.FILE_NAME_NO=1;
        }
        String dir = FileUtil.getSDPATH() + "Snapshot";
        String filename = no+ ".jpg";
        return FileUtil.createSDFile(dir, filename);
    }

}
