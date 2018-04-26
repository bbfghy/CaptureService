package com.hzj.captureservice.bean;

import java.util.Arrays;

/**
 * Created by hzj
 */

public class CmdInfo
{
    public int cmdId;
    public int cmdDataLen;

    public int obj_id;
    public String clientIP;
    public byte[] data = new byte[512];

    public void init()
    {
        cmdId = 0;
        cmdDataLen = 0;
        clientIP="";
        Arrays.fill(data,(byte)0);
    }

    public String toString()
    {
        return  "cmdId=" + cmdId+",clientIP="+clientIP;
    }
}
