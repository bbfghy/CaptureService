package com.hzj.captureservice.util;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * 位移优先级大于与操作
 * Char、byte、short类型，在进行移位之前，都将被转换成int类型，移位后的结果也是int类型；
 * 移位符号右边的操作数只截取其二进制的后5位（目的是防止因为移位操作而超出int类型的表示范围：
 * 2的5次方是32，int类型的最大范围是32位）；对long类型进行移位，结果仍然是long类型，移位符号右边的操作符只截取其二进制的后6位。
 */
public class ByteUtils {

    //=====================小端模式举例==========================
    // ps:int n=1234,那1为高位(高字节),4为低位(低字节)
    // ps:byte[] b = new byte[4],那b[0],b[1],b[2],b[3],地址分别是从低到高
    //--------------int2Bytes------------------
    // n >> 0  & 0xFF 取的是4    放到b[0]
    // n >> 8  & 0xFF 取的是3    放到b[1]
    // n >> 16 & 0xFF 取的是2    放到b[2]
    // n >> 24 & 0xFF 取的是1    放到b[3]
    // --------------bytes2Int------------------
    // (b[0] & 0xFF) << 0   取的是4    放到n0
    // (b[1] & 0xFF) << 8   取的是3    放到n1
    // (b[2] & 0xFF) << 16  取的是2    放到n2
    // (b[3] & 0xFF) << 24  取的是1    放到n3
    // int n = n1|n2|n3|n4;
    //==========================end============================

    /****************小端模式(低位放低地址)****************/
    public static byte[] short2Bytes(short s)
    {
        byte[] b = new byte[2];
        b[0] = (byte) (s >> 0 & 0xFF);
        b[1] = (byte) (s >> 8 & 0xFF);
        return b;
    }

    public static short bytes2Short(byte[] b)
    {
        short s = 0;
        if (b.length == 2)
        {
            int n = 0;
            n |= (b[0] & 0xFF) << 0;
            n |= (b[1] & 0xFF) << 8;
            s = (short) n;
        }
        return s;
    }

    public static byte[] int2Bytes(int n)
    {
        byte[] b = new byte[4];
        b[0] = (byte) (n >> 0 & 0xFF);
        b[1] = (byte) (n >> 8 & 0xFF);
        b[2] = (byte) (n >> 16 & 0xFF);
        b[3] = (byte) (n >> 24 & 0xFF);
        return b;
    }

    public static int bytes2Int(byte[] b)
    {
        int n = 0;
        if (b.length == 4)
        {
            n |= (b[0] & 0xFF) << 0;
            n |= (b[1] & 0xFF) << 8;
            n |= (b[2] & 0xFF) << 16;
            n |= (b[3] & 0xFF) << 24;
        }
        return n;
    }

    public static byte[] float2Bytes(float f)
    {
        return int2Bytes(Float.floatToRawIntBits(f));
    }

    public static float bytes2Float(byte[] b)
    {
        return Float.intBitsToFloat(bytes2Int(b));
    }

    /*****************字符串转字节***********************/
    public static byte[] stringToBytes(String s, int length)
    {
        while (s.getBytes().length < length)
        {
            s += "\0";
        }
        return s.getBytes();
    }

    /**
     * 从字节数组获取对象
     */
    public static Object getObjectFromBytes(byte[] objBytes) throws Exception
    {
        if (objBytes == null || objBytes.length == 0)
        {
            return null;
        }
        ByteArrayInputStream bi = new ByteArrayInputStream(objBytes);
        ObjectInputStream oi = new ObjectInputStream(bi);
        return oi.readObject();
    }

    /**
     * 从对象获取一个字节数组
     */
    public static byte[] getBytesFromObject(Serializable obj) throws Exception
    {
        if (obj == null)
        {
            return null;
        }
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        ObjectOutputStream oo = new ObjectOutputStream(bo);
        oo.writeObject(obj);
        return bo.toByteArray();
    }


    /**
     * 文件转化为字节数组
     */
    public static byte[] getBytesFromFile(File f)
    {
        if (f == null)
        {
            return null;
        }
        try
        {
            FileInputStream stream = new FileInputStream(f);
            ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            int n;
            while ((n = stream.read(b)) != -1)
            {
                out.write(b, 0, n);
            }
            stream.close();
            out.close();
            return out.toByteArray();
        }
        catch (IOException e)
        {
        }
        return null;
    }

    /**
     * 把字节数组保存为一个文件
     */
    public static File getFileFromBytes(byte[] b, String outputFile)
    {
        BufferedOutputStream stream = null;
        File file = null;
        try
        {
            file = new File(outputFile);
            FileOutputStream fstream = new FileOutputStream(file);
            stream = new BufferedOutputStream(fstream);
            stream.write(b);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (stream != null)
            {
                try
                {
                    stream.close();
                }
                catch (IOException e1)
                {
                    e1.printStackTrace();
                }
            }
        }
        return file;
    }

}
