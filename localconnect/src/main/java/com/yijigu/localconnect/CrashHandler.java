package com.yijigu.localconnect;

import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 打印错误日志
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "CrashHandler";
    public static CrashHandler instance = null;

    public static CrashHandler getInstance() {
        if (instance == null) {
            instance = new CrashHandler();
        }
        return instance;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        StringBuffer sb = printCrash(ex);
        Log.d(TAG, sb.toString());
    }

    public StringBuffer printCrash(Throwable ex) {
        StringBuffer sb = new StringBuffer();
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.flush();
        printWriter.close();
        String result = writer.toString();
        sb.append(result);
        return sb;

    }

}
