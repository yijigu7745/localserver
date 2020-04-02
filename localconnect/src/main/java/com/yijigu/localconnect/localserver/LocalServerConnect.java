package com.yijigu.localconnect.localserver;

import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.util.Log;

import com.google.gson.Gson;
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
//
//    private static final String TAG = "LocalServerConnect";
//
//    private static final Gson mGson = new Gson();
//
//    /**
//     * 客户端
//     */
//    private LocalSocket mClient = null;
//
//    /**
//     * 服务端
//     */
//    private LocalServerSocket mServer = null;
//
//    /**
//     * 与服务端链接中的客户端Map
//     */
//    private HashMap<String, LocalSocket> acceptList = new HashMap<>();
//
//    private ConnectHandler mHandler = null;
//
//    /**
//     * 连接地址
//     */
//    private LocalSocketAddress localSocketAddress;
//
////    /**
////     * 连接,使用单例模式
////     *
////     * @return
////     */
////    public static LocalServerConnect getInstance() {
////
////        if (mConnect == null) {
////            synchronized (LocalServerConnect.class) {
////                if (mConnect == null) {
////                    mConnect = new LocalServerConnect();
////                }
////            }
////        }
////
////        return mConnect;
////    }
//
//    /**
//     * 初始化客户端
//     *
//     * @param address 连接地址
//     */
//    public LocalServerConnect initClient(String address, ClientReceive clientReceive, String remark) {
//
//        if (mClient != null && mClient.isConnected()) {
//            Log.i(TAG, "<客户端连接中>");
//        } else {
//            mClient = new LocalSocket();
//            localSocketAddress = new LocalSocketAddress(address);
//            try {
//                //连接成功
//                mClient.connect(localSocketAddress);
//                read(address, mClient, clientReceive);
//                firstConnect(address, remark);
//                Log.i(TAG, "<客户端接入" + address + ">" + mClient.getFileDescriptor());
//            } catch (Exception e) {
//                Log.e(TAG, "<客户端连接" + address + "异常>\n"
//                        + e.getMessage());
//            }
//        }
//
//        return this;
//    }
//
//    /**
//     * 客户端发送消息
//     *
//     * @param address 连接地址
//     */
//    public LocalServerConnect initClient(String address, String remark) {
//        return initClient(address, null, remark);
//    }
//
//    /**
//     * 客户端读取消息
//     *
//     * @param address
//     * @param mClient
//     * @param clientReceive
//     */
//    private void read(String address, LocalSocket mClient, ClientReceive clientReceive) {
//        ExecutorUtil.doExecute(() -> {
//            try {
//                if (mClient.isConnected()) {
//                    InputStream is;
//                    InputStreamReader isr;
//                    BufferedReader br;
//                    is = mClient.getInputStream();
//                    isr = new InputStreamReader(is);
//                    br = new BufferedReader(isr);
//                    String resultStr;
//                    while ((resultStr = br.readLine()) != null) {
////                        String resultStr = br.readLine();
//                        //以换行符标识成结束
//                        if (StringUtils.isNotBlank(resultStr)) {
//                            Log.i(TAG, "<客户端" + mClient.getFileDescriptor()
//                                    + "接收到" + address + "服务端消息>" + resultStr);
//                            if (clientReceive != null && StringUtils.isNotBlank(resultStr)) {
//                                //处理服务端收到的消息
//                                clientReceive.dealMessage(resultStr);
//                            }
//                        }
//                    }
//                    Log.i(TAG, "read Null");
//                }
//            } catch (IOException e) {
//                Log.e(TAG, "<客户端" + mClient.getFileDescriptor() + "读取消息异常>\n"
//                        + e.getMessage());
//            }
//        });
//    }
//
//    /**
//     * 客户端第一次连接服务端,需要告知服务端,当前是哪个客户端连接的
//     *
//     * @param address
//     * @param remark
//     * @throws IOException
//     */
//    public void firstConnect(String address, String remark) {
//        MessageBean bean = new MessageBean();
//        bean.setType(MessageType.FIRST_CONNECT).setRemark(remark);
//        String message = mGson.toJson(bean);
//        OutputStream os;
//        OutputStreamWriter osw;
//        BufferedWriter bw;
//        try {
//            os = mClient.getOutputStream();
//            osw = new OutputStreamWriter(os);
//            bw = new BufferedWriter(osw);
//
//            Log.i(TAG, "<客户端" + mClient.getFileDescriptor()
//                    + "发送" + address + "消息开始>" + message);
//            if (StringUtils.isNotBlank(message)) {
//                bw.write(message);
//                bw.write("\n");
//                bw.flush();
//            }
//            Log.i(TAG, "<客户端" + mClient.getFileDescriptor()
//                    + "发送" + address + "消息结束>");
//        } catch (IOException e) {
//            Log.e(TAG, "<客户端" + mClient.getFileDescriptor() + "收发消息异常>\n"
//                    + e.getMessage());
//        }
//    }
//
//    /**
//     * 客户端发送消息
//     *
//     * @param address
//     * @param bean
//     * @throws IOException
//     */
//    public void send(String address, MessageBean bean) {
//        send(address, bean, 0);
//    }
//
//    /**
//     * 客户端发送消息
//     *
//     * @param address
//     * @param bean
//     * @throws IOException
//     */
//    public void send(String address, MessageBean bean, int count) {
//        String message = mGson.toJson(bean);
//        OutputStream os;
//        OutputStreamWriter osw;
//        BufferedWriter bw;
//        try {
//            os = mClient.getOutputStream();
//            osw = new OutputStreamWriter(os);
//            bw = new BufferedWriter(osw);
//
//            Log.i(TAG, "<客户端" + ((mClient == null) ? "mClient is Null" :
//                    mClient.getFileDescriptor().toString()) + "-" + bean.getRemark()
//                    + "发送" + address + "消息开始>" + message);
//            if (StringUtils.isNotBlank(message)) {
//                bw.write(message);
//                bw.write("\n");
//                bw.flush();
//            }
//            Log.i(TAG, "<客户端" + ((mClient == null) ? "mClient is Null" :
//                    mClient.getFileDescriptor().toString()) + "-" + bean.getRemark()
//                    + "发送" + address + "消息结束>");
//        } catch (Exception e) {
//            if (mClient != null) {
//                try {
//                    mClient.close();
//                } catch (IOException e1) {
//                    e1.printStackTrace();
//                }
//                mClient = null;
//            }
//            if (count < 5) {
//                initClient(address, bean.getRemark());
//                send(address, bean, ++count);
//            }
//            Log.e(TAG, "<客户端" + ((mClient == null) ? "mClient is Null" :
//                    mClient.getFileDescriptor().toString()) + "-" + bean.getRemark()
//                    + "发送消息异常>\n" + e.getMessage());
//        }
//    }
//
//    public interface ClientReceive {
//        void dealMessage(String message);
//
//    }
//
//    /**
//     * 服务端接收消息
//     *
//     * @param address 服务地址
//     * @return
//     */
//    public boolean startServer(String address, ConnectHandler handler) {
//
//        try {
//            mServer = new LocalServerSocket(address);
//        } catch (IOException e) {
//            return false;
//        }
//        this.mHandler = handler;
//        mHandler.bind(this);
//        ExecutorUtil.doExecuteForLocalSocket(() -> {
//            for (; ; ) {
//                Log.i(TAG, "<服务端" + address + "开始监听连接请求>");
//                try {
//                    LocalSocket accept = mServer.accept();
//                    if (accept == null) {
//                        Log.i(TAG, "<建立连接失败>");
//
//                        try {
//                            Thread.sleep(1000);
//                        } catch (InterruptedException e) {
//                        }
//                        continue;
//                    }
//                    serverReceive(address, accept);
//                } catch (Exception e) {
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e2) {
//                    }
//                    Log.e(TAG, "<建立连接" + address + "异常>\n"
//                            + e.getMessage());
//                }
//                Log.i(TAG, "<服务端" + address + "已建立一个连接>");
//            }
//        });
//        return true;
//    }
//
//    /**
//     * 服务端响应客户端请求
//     *
//     * @param address
//     */
//    public boolean serverSend(String address, MessageBean bean) {
//        LocalSocket accept = acceptList.get(address);
//        if (accept == null) {
//            Log.e(TAG, "当前地址没有连接中的客户端");
//            return false;
//        }
//        String message = mGson.toJson(bean);
//        try {
//            //获取输出流,往客户端发送消息
//            OutputStream os = accept.getOutputStream();
//            OutputStreamWriter osr = new OutputStreamWriter(os);
//            BufferedWriter bw = new BufferedWriter(osr);
//            //获取输出流,往客户端发送消息
//            Log.i(TAG, "<往客户端" + address + "-" + accept.getFileDescriptor()
//                    + "发送消息开始>");
//            if (StringUtils.isNotBlank(message)) {
//                bw.write(message);
//                bw.write("\n");
//                bw.flush();
//                Log.i(TAG, "<往客户端" + address + "-" + accept.getFileDescriptor()
//                        + "发送了消息>" + message);
//            }
//            Log.i(TAG, "<往客户端" + address + "-" + accept.getFileDescriptor()
//                    + "发送消息结束>");
//        } catch (Exception e) {
//            Log.e(TAG, "<往客户端" + address + "-" + accept.getFileDescriptor()
//                    + "发送消息失败>\n"
//                    + e.getMessage());
//            return false;
//        }
//        return true;
//    }
//
//    /**
//     * 服务端接收客户端消息
//     *
//     * @param address
//     */
//    private void serverReceive(String address, LocalSocket accept) {
//        ExecutorUtil.doExecute(() -> {
//            try {
//                InputStream is = accept.getInputStream();
//                InputStreamReader isr = new InputStreamReader(is);
//                BufferedReader br = new BufferedReader(isr);
//
//                //接收到的消息
//                while (true) {
//                    String resultStr = br.readLine();
//                    //以换行符标识成结束
//                    if (StringUtils.isNotBlank(resultStr)) {
//                        Log.i(TAG, "<服务端" + address + "接收到了" + accept.getFileDescriptor() + "的消息>" + resultStr);
//                        //设置消息内容
//                        decodeMessage(resultStr, accept);
//                    }
//                }
//            } catch (Exception e) {
//                Log.e(TAG, "<服务端" + address + "接收消息异常>\n"
//                        + e.getMessage());
//            }
//        });
//    }
//
//    /**
//     * 解析消息
//     *
//     * @param resultStr
//     */
//    private void decodeMessage(String resultStr, LocalSocket accept) {
//        try {
//            JSONObject jsonObject = new JSONObject(resultStr);
//            int type = jsonObject.getInt("type");
//            String remark = jsonObject.getString("remark");
//
//            acceptList.put(remark, accept);
//            if (mHandler != null) {
//                mHandler.handleMessage(type, resultStr);
//            } else {
//                Log.e(TAG, "处理消息失败, handler为空");
//            }
//        } catch (Exception e) {
//            Log.e(TAG, "<解析消息失败>\n"
//                    + e.getMessage());
//        }
//    }

}
