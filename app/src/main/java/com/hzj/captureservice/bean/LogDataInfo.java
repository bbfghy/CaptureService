
package com.hzj.captureservice.bean;

/**
 * Created by hzj
 */

public class LogDataInfo
{
    public StringBuilder logContent;
    public int data_len;

    public LogDataInfo()
    {
        logContent = null;
        data_len = 0;
    }

    public void init() {
        logContent=new StringBuilder();
        data_len = 0;
    }
}
