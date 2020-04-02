package com.yijigu.localconnect.localserver;

public class KeepAliveBean {
    private long count;
    private long preTime;
    private long nowTime;

    public long getCount() {
        return count;
    }

    public KeepAliveBean setCount(long count) {
        this.count = count;
        return this;
    }

    public long getPreTime() {
        return preTime;
    }

    public KeepAliveBean setPreTime(long preTime) {
        this.preTime = preTime;
        return this;
    }

    public long getNowTime() {
        return nowTime;
    }

    public KeepAliveBean setNowTime(long nowTime) {
        this.nowTime = nowTime;
        return this;
    }
}
