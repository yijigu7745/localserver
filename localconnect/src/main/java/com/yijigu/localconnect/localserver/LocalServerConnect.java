package com.yijigu.localconnect.localserver;

import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.util.Log;

import com.google.gson.Gson;
import com.yijigu.localconnect.CrashHandler;
import com.yijigu.localconnect.ExecutorUtil;
import com.yijigu.localconnect.StringUtils;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;

/**
 * @author Administrator
 */
public class LocalServerConnect {

    private static final String TAG = "LocalServerConnect";

    private static final Gson mGson = new Gson();

    /**
     * 客户端列表Map
     */
    private HashMap<String, LocalSocket> clientList = new HashMap<>();

    /**
     * 服务端
     */
    private LocalServerSocket mServer = null;

    /**
     * 与服务端链接中的客户端Map
     */
    private HashMap<String, LocalSocket> acceptList = new HashMap<>();

    public void setHandler(ConnectHandler mHandler) {
        this.mHandler = mHandler;
    }

    private ConnectHandler mHandler = null;

    /**
     * 连接地址
     */
    private LocalSocketAddress localSocketAddress;

    private volatile static LocalServerConnect mConnect;

    /**
     * 连接,使用单例模式
     *
     * @return
     */
    public static LocalServerConnect getInstance() {

        if (mConnect == null) {
            synchronized (LocalServerConnect.class) {
                if (mConnect == null) {
                    mConnect = new LocalServerConnect();
                }
            }
        }
        return mConnect;
    }

    /**
     * 初始化客户端
     *
     * @param address 连接地址
     */
    public LocalServerConnect initClient(String address, ClientReceive clientReceive) {

        LocalSocket mClient = clientList.get(address);
        if (mClient != null && mClient.isConnected()) {
            Log.i(TAG, "<客户端连接中>");
        } else {
            mClient = new LocalSocket();
            localSocketAddress = new LocalSocketAddress(address);
            try {
                //连接成功,将客户端实例放入Map里
                mClient.connect(localSocketAddress);
                clientList.put(address, mClient);
                read(address, mClient, clientReceive);
                Log.i(TAG, "<客户端接入" + address + ">");
            } catch (Exception e) {
                Log.e(TAG, "<客户端连接" + address + "异常>\n"
                        + e.getMessage() + "\n"
                        + CrashHandler.getInstance().printCrash(e));
                //连接失败或异常,从Map中移除键
                clientList.remove(address);
            }
        }

        return getInstance();
    }

    /**
     * 客户端发送消息
     *
     * @param address 连接地址
     */
    public LocalServerConnect initClient(String address) {
        return initClient(address, null);
    }

    /**
     * 客户端读取消息
     *
     * @param address
     * @param mClient
     * @param clientReceive
     */
    private void read(String address, LocalSocket mClient, ClientReceive clientReceive) {
        ExecutorUtil.doExecute(() -> {
            try {
                InputStream is;
                InputStreamReader isr;
                BufferedReader br;
                is = mClient.getInputStream();
                isr = new InputStreamReader(is);
                br = new BufferedReader(isr);
                while (true) {
                    String resultStr = br.readLine();
                    //以换行符标识成结束
                    if (StringUtils.isNotBlank(resultStr)) {
                        Log.i(TAG, "<客户端" + mClient.getFileDescriptor()
                                + "接收到" + address + "服务端消息>" + resultStr);
                        if (clientReceive != null && StringUtils.isNotBlank(resultStr)) {
                            //处理服务端收到的消息
                            clientReceive.dealMessage(resultStr);
                        }
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "<客户端" + mClient.getFileDescriptor() + "收发消息异常>\n"
                        + e.getMessage() + "\n"
                        + CrashHandler.getInstance().printCrash(e));
            }
        });
    }

    /**
     * 客户端发送消息
     *
     * @param address
     * @param bean
     * @throws IOException
     */
    public void send(String address, MessageBean bean) {
        LocalSocket client = mConnect.clientList.get(address);
        if (client == null) {
            initClient(address);
            client = mConnect.clientList.get(address);
            if (client == null) {
                Log.i(TAG, "发送失败: 客户端初始化失败");
                return;
            }
        }
        String message = mGson.toJson(bean);
        OutputStream os;
        OutputStreamWriter osr;
        BufferedWriter bw;
        try {
            os = client.getOutputStream();
            osr = new OutputStreamWriter(os);
            bw = new BufferedWriter(osr);

            Log.i(TAG, "<客户端" + client.getFileDescriptor()
                    + "发送" + address + "消息开始>" + message);
            bw.write(message);
            bw.write("\n");
            bw.flush();
            Log.i(TAG, "<客户端" + client.getFileDescriptor()
                    + "发送" + address + "消息结束>");
        } catch (IOException e) {
            Log.e(TAG, "<客户端" + client.getFileDescriptor() + "收发消息异常>\n"
                    + e.getMessage() + "\n"
                    + CrashHandler.getInstance().printCrash(e));
        }
    }

    public interface ClientReceive {
        void dealMessage(String message);
    }

    /**
     * 服务端接收消息
     *
     * @param address 服务地址
     * @return
     */
    public boolean startServer(String address, ConnectHandler handler) {

        try {
            mServer = new LocalServerSocket(address);
        } catch (IOException e) {
            return false;
        }
        setHandler(handler);
        ExecutorUtil.doExecuteForLocalSocket(() -> {
            for (; ; ) {
                Log.i(TAG, "<服务端" + address + "开始监听连接请求>");
                try {
                    LocalSocket accept = mServer.accept();
                    if (accept == null) {
                        Log.i(TAG, "<建立连接失败>");

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                        }
                        continue;
                    }
                    acceptList.put(address, accept);
                    serverReceive(address);
                } catch (Exception e) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e2) {
                    }
                    Log.e(TAG, "<建立连接" + address + "异常>\n"
                            + e.getMessage() + "\n"
                            + CrashHandler.getInstance().printCrash(e));
                }
                Log.i(TAG, "<服务端" + address + "已建立一个连接>");
            }
        });
        return true;
    }

    /**
     * 服务端响应客户端请求
     *
     * @param address
     */
    private void serverSend(String address) {
        LocalSocket accept = acceptList.get(address);
        if (accept == null || accept.isConnected()) {
            Log.e(TAG, "当前地址没有连接中的客户端");
            return;
        }
        try {
            //获取输出流,往客户端发送消息
            OutputStream os = accept.getOutputStream();
            OutputStreamWriter osr = new OutputStreamWriter(os);
            BufferedWriter bw = new BufferedWriter(osr);
            //获取输出流,往客户端发送消息
            Log.i(TAG, "<服务端" + address + "发送消息开始>");
            if (mHandler != null) {
                if (StringUtils.isNotBlank(mHandler.getMessage())) {
                    bw.write(mHandler.getMessage());
                    mHandler.clearMessage();
                }
            } else {
                Log.e(TAG, "回复消息失败, handler为空");
            }
            bw.write("\n");
            bw.flush();
            Log.i(TAG, "<服务端" + address + "发送消息结束>");
        } catch (Exception e) {
            Log.e(TAG, "<服务端" + address + "往客户端发送消息失败>\n"
                    + e.getMessage() + "\n"
                    + CrashHandler.getInstance().printCrash(e));
        }
    }

    /**
     * 服务端接收客户端消息
     *
     * @param address
     */
    private void serverReceive(String address) {
        //从连接Map中取出对应地址的连接
        LocalSocket accept = acceptList.get(address);
        if (accept == null || accept.isConnected()) {
            Log.e(TAG, "当前地址没有连接中的客户端");
            return;
        }
        ExecutorUtil.doExecute(() -> {
            try {
                InputStream is = accept.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);

                //接收到的消息
                while (true) {
                    String resultStr = br.readLine();
                    //以换行符标识成结束
                    if (StringUtils.isNotBlank(resultStr)) {
                        Log.i(TAG, "<服务端" + address + "接收到了消息>" + resultStr);
                        //设置消息内容
                        decodeMessage(resultStr);
                        //响应消息
                        serverSend(address);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "<服务端" + address + "接收消息异常>\n"
                        + e.getMessage() + "\n"
                        + CrashHandler.getInstance().printCrash(e));
            }
        });
    }

    /**
     * 解析消息
     *
     * @param resultStr
     */
    private void decodeMessage(String resultStr) {
        try {
            JSONObject jsonObject = new JSONObject(resultStr);
            int type = jsonObject.getInt("type");
            if (mHandler != null) {
                mHandler.handleMessage(type, resultStr);
            } else {
                Log.e(TAG, "处理消息失败, handler为空");
            }
        } catch (Exception e) {
            Log.e(TAG, "<解析消息失败>\n"
                    + e.getMessage() + "\n"
                    + CrashHandler.getInstance().printCrash(e));
        }
    }

}
