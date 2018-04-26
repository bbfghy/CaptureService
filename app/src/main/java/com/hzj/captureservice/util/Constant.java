package com.hzj.captureservice.util;

/**
 * 公共类：包括常量、公共变量 方便配置调整
 * Created by Hzj
 */
public class Constant {

    /** file name serial number */
    public static int FILE_NAME_NO=1;
    public static final int MAX_FILE_NAME_NO = 50000;

    /**handler message id*/
    public static final int HANDLER_SNAPSHOT_CMD = 10;
    public static final int HANDLER_SAVE_SNAPSHOT_MSGID = 11;

    /** activity request code */
    public static final int REQUEST_MEDIA_PROJECTION = 101;

    /** broadcast action */
    public static final String BROADCAST_ACTION_SCREEN_CAPTURE = "BROADCAST_ACTION_SCREEN_CAPTURE";

    /**当前sd卡上缓存的图片数量*/
    public static int SD_PICTURE_COUNT=0;
    public static final int MAX_PICTURE_COUNT = 30;
    public static int MAX_DELETE_COUNT=25;
    public static final String PREKEY_PICTURE_COUNT = "pic_count";
}
