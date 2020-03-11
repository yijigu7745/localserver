package com.yijigu.localconnect.localserver;

/**
 * 通讯用到的消息Bean
 */
public class MessageBean<T> {

    private int type;
    private String content = "";
    private String remark = "";
    private int code;
    private T missionInfo;

    public T getMissionInfo() {
        return missionInfo;
    }

    public MessageBean setMissionInfo(T missionInfo) {
        this.missionInfo = missionInfo;
        return this;
    }

    public int getType() {
        return type;
    }

    public MessageBean setType(int type) {
        this.type = type;
        return this;
    }

    public String getContent() {
        return content;
    }

    public MessageBean setContent(String content) {
        this.content = content;
        return this;
    }

    public String getRemark() {
        return remark;
    }

    public MessageBean setRemark(String remark) {
        this.remark = remark;
        return this;
    }

    public int getCode() {
        return code;
    }

    public MessageBean setCode(int code) {
        this.code = code;
        return this;
    }
}
