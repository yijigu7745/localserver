package com.yijigu.localconnect.localserver;

/**
 * 通讯消息类型
 */
public class MessageType {

    /**
     * 模拟点击登录Shopee
     */
    public static final int LOGIN_DONE = 1;

    /**
     * Shopee登录状态
     */
    public static final int LOGIN_STATUS = 2;

    /**
     * 新机失败
     */
    public static final int NEW_PHONE_FAILED = 999;

    /**
     * 新机检测当前登录状态
     */
    public static final int REGISTER_STATUS = 101;

    /**
     * 获取新机信息
     */
    public static final int GET_PHONE_STATE = 102;

    /**
     * 上报订单
     */
    public static final int REPORT_ORDER = 103;

    /**
     * 上报订单编号
     */
    public static final int REPORT_ORDER_SN = 104;
}
