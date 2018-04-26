package com.hzj.captureservice.bean;

import android.graphics.Bitmap;
import android.media.Image;

/**
 * TFFScreenCapture
 * Created by Hzj on 2018/4/9.
 */

public class CmdSaveInfo {

    private int cmdId;
    private String sourceIp;
    private Image mImage;

    private long startTime;

    private int fileNameNo;
    private Bitmap bitmap;

    public Bitmap getBitmap() {
        return bitmap;
    }

    public int getFileNameNo() {
        return fileNameNo;
    }

    public void setFileNameNo(int fileNameNo) {
        this.fileNameNo = fileNameNo;
    }


    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public Image getmImage() {
        return mImage;
    }

    public void setmImage(Image mImage) {
        this.mImage = mImage;
    }

    public int getCmdId() {
        return cmdId;
    }

    public void setCmdId(int cmdId) {
        this.cmdId = cmdId;
    }

    public void init() {
        cmdId=0;
        sourceIp=null;
        mImage=null;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
