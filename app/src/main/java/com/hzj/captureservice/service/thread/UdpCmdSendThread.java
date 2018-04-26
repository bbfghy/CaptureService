package com.hzj.captureservice.service.thread;

import com.hzj.captureservice.bean.SendDataInfo;
import com.hzj.captureservice.queue.ObjectManager;
import com.hzj.captureservice.util.Command;
import com.hzj.captureservice.util.NetUtil;
import com.hzj.captureservice.util.LogUtil;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Arrays;

/**
 * 发送截图
 * Created by Hzj
 */
public class UdpCmdSendThread extends Thread {

    private boolean isRunning=true;
    private String TAG=UdpCmdSendThread.class.getSimpleName();

    DatagramSocket socketSendCmd=null;

    public UdpCmdSendThread()
    {
        try
        {
            socketSendCmd = new DatagramSocket(null);
            socketSendCmd.setReuseAddress(true);
            socketSendCmd.bind(new InetSocketAddress(Command.UDP_SEND_SNAPSHOT_PORT));
        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }


    @Override
    public void run()
    {
       while (isRunning)
       {
           SendDataInfo cmd=null;
           try
           {
               cmd= ObjectManager.allocateSendCmdInfo();
               long t=System.currentTimeMillis();

               String filePath=cmd.path;

               String nativeIp= NetUtil.getHostIP();
               StringBuffer sb=new StringBuffer();
               sb.append("http://").append(nativeIp).append(":").append(Command.WEB_PORT).append(filePath);
               String url=sb.toString();

               byte[] datas=url.getBytes();
               int maxLen=datas.length;
               byte[] data=new byte[Command.RESPONSE_URL_NAME_OFFSET+maxLen];
               System.arraycopy(datas,0,data,Command.RESPONSE_URL_NAME_OFFSET,maxLen);
               DatagramPacket packet = new DatagramPacket(data, data.length);
               data=getPacketHeadData(data,maxLen);

               packet.setPort(Command.UDP_SEND_CLIENT_SNAPSHOT_PORT);
               packet.setAddress(InetAddress.getByName(cmd.ip));
               socketSendCmd.send(packet);
               Arrays.fill(data,(byte)0);// 清除数据
               LogUtil.d(TAG,"send cost :"+(System.currentTimeMillis()-t));
           }
           catch (Exception e)
           {
               e.printStackTrace();
               LogUtil.e(TAG,"SendCmdThread.run() call socketSendCmd.send() failed,catch Exception");
           }
       }
    }

    /**
     * 消息命令(头+体),注意append数据的顺序和接口文档要保持一致.
     * @return
     */
    private byte[] getPacketHeadData(byte[] data,int maxlen)
    {
        // 命令头
        data[Command.CMD_HEAD_BYVENDORFLAG_OFFSET] = (byte) 255;
        data[Command.CMD_HEAD_UCMDID_OFFSET] = (byte) (Command.CMD_SEND_SNAPSHOT_ID >> 0 & 0xFF);
        data[Command.CMD_HEAD_UCMDID_OFFSET + 1] = (byte) (Command.CMD_SEND_SNAPSHOT_ID >> 8 & 0xFF);
        data[Command.RESPONSE_URL_LENGTH_OFFSET ] = (byte) (maxlen >> 0 & 0xFF);
        data[Command.RESPONSE_URL_LENGTH_OFFSET + 1] = (byte) (maxlen >> 8 & 0xFF);
        return data;
    }

    public void closeSockect()
    {
        if (socketSendCmd!=null)
            socketSendCmd.close();
    }

    public void setRunning(boolean running) {
        this.isRunning = running;
        closeSockect();
    }
}
