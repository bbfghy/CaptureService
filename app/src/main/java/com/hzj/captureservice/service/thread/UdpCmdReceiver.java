
package com.hzj.captureservice.service.thread;

import com.hzj.captureservice.bean.CmdInfo;
import com.hzj.captureservice.queue.ObjectManager;
import com.hzj.captureservice.util.ByteUtils;
import com.hzj.captureservice.util.Command;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 接收命令通信线程
 * Created by Hzj
 */
public class UdpCmdReceiver extends Thread {

    private boolean isRunning = true;
    private static Lock effectiveIpLock = new ReentrantLock();
    private String TAG=UdpCmdReceiver.class.getSimpleName();
    DatagramSocket socketReceiveCmd=null;

    @Override
    public void run()
    {
        byte[] data = new byte[Command.CMD_HEAD_LENGTH];
        DatagramPacket packetReceive = new DatagramPacket(data, data.length);
        CmdInfo cmdInfo = null;

        try
        {
            socketReceiveCmd = new DatagramSocket(null);
            socketReceiveCmd.setReuseAddress(true);
            socketReceiveCmd.bind(new InetSocketAddress(Command.UDP_RECEIVE_SNAPSHOT_PORT));
            while (isRunning)//UdpCmdReceiver
            {
                socketReceiveCmd.receive(packetReceive);//阻塞,有命令进来才会有接收数据
                String ip = isEffectiveIp(packetReceive.getAddress().toString());//校验ip
                if (ip == null) continue;
                //解析数据
                byte[] bytes = packetReceive.getData();// 获取接收到的数据
                short cmd  = ByteUtils.bytes2Short(new byte[]{bytes[Command.CMD_HEAD_UCMDID_OFFSET], bytes[Command.CMD_HEAD_UCMDID_OFFSET + 1]});//命令ID
                if ( cmd == Command.CMD_RECEIVE_SNAPSHOT_ID)
                {
                    cmdInfo = ObjectManager.allocateCmdInfoObj();// UdpCmdReceiver
                    cmdInfo.clientIP=ip;
                    cmdInfo.cmdId=cmd;

                    /*****主业务逻辑处理*****/
                    // 接收一个截图指令
                    ObjectManager.snapshot_queue.put(cmdInfo);
                    cmdInfo = null;

                    Arrays.fill(data,(byte)0);// 清除数据

                    if (cmdInfo != null)
                    {
                        ObjectManager.freeCmdInfoQueueObj(cmdInfo);// 归还对象池
                        cmdInfo = null;
                    }
                }
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    /**
     * 确保ip有效
     */
    private String isEffectiveIp(String ip)
    {
        String eIp;
        effectiveIpLock.lock();
        if (ip != null)
        {
            eIp = ip.replace("/", "");
            if (eIp.equals("::1")) eIp = null;
        }
        else
        {
            eIp = null;
        }
        effectiveIpLock.unlock();
        return eIp;
    }

    public void setRunning(boolean running)
    {
        this.isRunning = running;
        if (!isRunning)
        {
            try
            {
                socketReceiveCmd.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
