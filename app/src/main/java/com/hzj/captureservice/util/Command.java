package com.hzj.captureservice.util;

/**
 * Created by Hzj
 */
public class Command {

    //define app send event

    /***** define cmd head **/
    public static final int CMD_HEAD_LENGTH = 13;

    public static final int CMD_HEAD_BYVENDORFLAG_OFFSET = 0; // 1 byte 固定值为255,截图标识(1个字节)
    public static final int CMD_HEAD_UUSERID_OFFSET = 1;  // 2 byte 用户ID,暂时没用(2字节)
    public static final int CMD_HEAD_UCMDID_OFFSET = 3;  // 2 byte 命令ID(2字节)
    public static final int CMD_HEAD_UCMDINDEX_OFFSET = 5; // 2 byte 命令编号ID(2字节),没有数据则默认cmdIndex=100;
    public static final int CMD_HEAD_ULEN_OFFSET = 7; // 4 byte 数据长度(4字节)
    public static final int CMD_HEAD_URESULT_OFFSET = 11; // 1 byte 返回结果0-失败,1-成功,2-无效指令(1字节),3-不能抢占,4-PIN码不正确
    public static final int CMD_HEAD_URESERVE_OFFSET = 12; // 1 byte 保留(1字节)
    public static final int CMD_HEAD_DATA_OFFSET = 13;

    //define SNAPSHOT_CMD_ID cmd receive body

    /***** define SNAPSHOT_CMD_ID response play body **/
    public static final int RESPONSE_URL_LENGTH_OFFSET = 13; // 2 byte
    public static final int RESPONSE_URL_NAME_OFFSET = 15;  // 4 byte

    /***** snapshot cmd *****/
    public static final short CMD_RECEIVE_SNAPSHOT_ID = 301;//receive no body
    public static final short CMD_SEND_SNAPSHOT_ID = 302;//send have body

    /****  save snapshot cmd ****/
    public static final short CMD_SAVE_SNAPSHOT_ID = 303;

    /***** web port *****/
    public static final short WEB_PORT = 9001;

    /***** upd socket port *****/
    public static final short UDP_RECEIVE_SNAPSHOT_PORT = 9002;
    public static final short UDP_SEND_SNAPSHOT_PORT = 9003;
    public static final short UDP_SEND_CLIENT_SNAPSHOT_PORT = 9004;


}
