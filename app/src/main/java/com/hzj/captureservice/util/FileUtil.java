
package com.hzj.captureservice.util;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by hzj
 */

public class FileUtil {
    private Lock lock = new ReentrantLock(true);//防止多线程中的异常导致读写不同步问题的lock
    private static String SDPATH;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * 检查应用程序是否允许写入存储设备
     * 如果应用程序不允许那么会提示用户授予权限
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity)
    {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED)
        {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }


    public static String getSDPATH()
    {
        if (isSdcardMounted())
        {
            SDPATH = Environment.getExternalStorageDirectory() + "/";
        }
        else
        {
            SDPATH = Environment.getDataDirectory() + "/";
        }
        return SDPATH;
    }

    /**
     * 是否可在SD卡可读写文件
     */
    public static boolean isSdcardMounted()
    {
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    private static boolean isNotEmpty(String str)
    {
        return str != null && !"".equals(str.trim());
    }

    public static File createSDFile(String filePath)
    {
        if (TextUtils.isEmpty(filePath)) return null;
        File file = new File(filePath);
        try
        {
            file.getParentFile().mkdirs();// 创建目录，如果目录已经存在,则会跳过
            file.createNewFile();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
        return file;
    }

    public static File createSDFile(String dirPath, String fileName)
    {
        if (TextUtils.isEmpty(dirPath) || TextUtils.isEmpty(fileName)) return null;

        File file = new File(dirPath, fileName);
        try
        {
            file.getParentFile().mkdirs();// 创建目录，如果目录已经存在,则跳过
            file.createNewFile();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
        return file;
    }


    /**
     * 追加或者覆盖写入文件<br>
     * 文件不存在则创建<br>
     * 文件已存则判断是否追加，不追加则覆盖<br>
     *
     * @param fileName
     * @param content
     * @param isAppend
     */
    public void fileAppendOrCoverWrite(String fileName, String content, boolean isAppend)
    {
        try
        {
            lock.tryLock();
            // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
            FileWriter writer = new FileWriter(fileName, isAppend);
            writer.write(content);
            writer.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            lock.unlock();
        }
    }

    /**
     * 追加写入文件,如果该文件,则会先创建文件
     *
     * @param fileName
     * @param content
     */
    public void fileAppendWrite(String fileName, String content)
    {
        fileAppendOrCoverWrite(fileName, content, true);
    }

    /**
     * 覆盖写入文件,如果该文件,则会先创建文件
     *
     * @param fileName
     * @param content
     */
    public void fileCoverWrite(String fileName, String content)
    {
        fileAppendOrCoverWrite(fileName, content, false);
    }

    public static void mkSDDir(String dirpath)
    {
        if (isSdcardMounted() && isNotEmpty(dirpath))
        {
            File dir = new File(dirpath);
            if (!dir.exists())
            {
                dir.mkdirs();
            }
        }
    }

    /**
     * 删除一个目录（以及目录下的所有文件）
     */
    public boolean delSDDir(String dirpath)
    {
        return delSDDir(new File(dirpath));
    }

    /**
     * 删除一个目录（以及目录下的所有文件）
     */
    public boolean delSDDir(File dir)
    {
        if (dir == null || !dir.exists() || dir.isFile())
        {
            return false;
        }
        if (!dir.canRead()) return false;// 如果没有读权限则返回false
        for (File file : dir.listFiles())
        {
            if (file.isFile())
            {
                file.delete();
            }
            else if (file.isDirectory())
            {
                delSDDir(file);// 递归
            }
        }
        dir.delete();
        return !dir.exists();
    }


    /**
     * 删除指定目录下文件及目录
     *
     * @param deleteThisPath
     * @return
     */
    public static void deleteFolderFile(String filePath, boolean deleteThisPath)
    {
        if (!TextUtils.isEmpty(filePath))
        {
            try
            {
                File file = new File(filePath);
                if (file.isDirectory())
                {// 处理目录
                    File files[] = file.listFiles();
                    for (int i = 0; i < files.length; i++)
                    {
                        deleteFolderFile(files[i].getAbsolutePath(), true);
                    }
                }
                if (deleteThisPath)
                {
                    if (!file.isDirectory())
                    {// 如果是文件，删除
                        file.delete();
                    }
                    else
                    {// 目录
                        if (file.listFiles().length == 0)
                        {// 目录下没有文件或者目录，删除
                            file.delete();
                        }
                    }
                }
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取文件夹大小
     *
     * @param file File实例
     * @return long
     */
    public static long getFolderSize(File file)
    {
        long size = 0;
        if (!file.exists())
        {
            return size;
        }
        try
        {
            File[] fileList = file.listFiles();
            for (int i = 0; i < fileList.length; i++)
            {
                if (fileList[i].isDirectory())
                {
                    size = size + getFolderSize(fileList[i]);

                }
                else
                {
                    size = size + fileList[i].length();

                }
            }
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //return size/1048576;
        return size;
    }

    /**
     * 格式化单位
     *
     * @param size
     * @return
     */
    public static String getFormatSize(double size)
    {
        double kiloByte = size / 1024;
        if (kiloByte < 1)
        {
            return /*size + */"0KB";
        }

        double megaByte = kiloByte / 1024;
        if (megaByte < 1)
        {
            BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
            return result1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "KB";
        }

        double gigaByte = megaByte / 1024;
        if (gigaByte < 1)
        {
            BigDecimal result2 = new BigDecimal(Double.toString(megaByte));
            return result2.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "MB";
        }

        double teraBytes = gigaByte / 1024;
        if (teraBytes < 1)
        {
            BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));
            return result3.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "GB";
        }
        BigDecimal result4 = new BigDecimal(teraBytes);
        return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "TB";
    }

}
