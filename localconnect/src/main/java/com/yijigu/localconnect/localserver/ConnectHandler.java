package com.yijigu.localconnect.localserver;

public abstract class ConnectHandler implements HandlerMessage {
    String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
