package com.yijigu.localconnect.localserver;

public abstract class ConnectHandler implements HandlerMessage {
    String message;
    LocalSocketServer mConnect;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void clearMessage() {
        message = null;
    }

    public void bind(LocalSocketServer mServer){
        this.mConnect = mServer;
    }

    public LocalSocketServer getBindConnect() {
        return mConnect;
    }
}
