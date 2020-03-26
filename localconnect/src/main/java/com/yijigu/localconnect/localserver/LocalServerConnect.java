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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class LocalServerConnect {

    private static final String TAG = "LocalServerConnect";

    private static final Gson mGson = new Gson();

    /**
     * 客户端
     */
    private static LocalSocket mClient = null;

    /**
     * 服务端
     */
    private static LocalServerSocket mServer = null;

    public static void setHandler(ConnectHandler mHandler) {
        LocalServerConnect.mHandler = mHandler;
    }

    private static ConnectHandler mHandler = null;

    /**
     * 连接地址
     */
    private static LocalSocketAddress localSocketAddress;

    /**
     * 检测连接是否正常
     *
     * @param address
     * @return
     */
    public static boolean testConnect(String address) {
        mClient = new LocalSocket();
        OutputStream os = null;
        try {
            localSocketAddress = new LocalSocketAddress(address);
            mClient.connect(localSocketAddress);
            if (!mClient.isConnected()) {
                return false;
            }
            os = mClient.getOutputStream();
            String test = "{\"type\":-999,\"content\":\"test\",\"remark\":\"test\"}";
            os.write(test.getBytes());
            os.write(0);
        } catch (IOException e) {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException eos) {
                    eos.printStackTrace();
                }
            }
            return false;
        }
        return true;
    }

    /**
     * 客户端发送消息
     *
     * @param address 连接地址
     * @param bean    消息内容
     */
    public static void clientSend(String address, MessageBean bean, int retry) {

        String message = mGson.toJson(bean);
        ExecutorUtil.doExecute(() -> {
            Log.i(TAG, "<客户端" + bean.getRemark() + "发送开始>");
            mClient = new LocalSocket();
            InputStream is = null;
            OutputStream os = null;
            boolean isError = false;
            try {
                localSocketAddress = new LocalSocketAddress(address);
                mClient.connect(localSocketAddress);

                if (!mClient.isConnected()) {
                    Log.i(TAG, "<客户端" + bean.getRemark() + "连接" + address + "失败>");
                    return;
                }

                is = mClient.getInputStream();
                os = mClient.getOutputStream();

                Log.i(TAG, "<客户端" + bean.getRemark() + "发送" + address + "消息开始>" + message);
                os.write(message.getBytes());
                os.write(0);
                os.flush();
                Log.i(TAG, "<客户端" + bean.getRemark() + "发送" + address + "消息结束>");
                Log.i(TAG, "<客户端" + bean.getRemark() + "接收" + address + "消息开始>");
                try {
                    String resultStr = null;
                    int readed = is.read();
                    int size = 0;
                    byte[] bytes = new byte[0];
                    while (readed != -1) {
                        byte[] copy = new byte[10240];
                        System.arraycopy(bytes, 0, copy, 0, bytes.length);
                        bytes = copy;
                        bytes[size++] = (byte) readed;
                        //以换行符标识成结束
                        if ('\0' == (byte) readed) {
                            resultStr = new String(bytes, 0, size - 1);
                            break;
                        }
                        readed = is.read();
                    }
                    Log.i(TAG, "<客户端" + bean.getRemark() + "接收到" + address + "服务器消息>" + resultStr);
                } catch (IOException e) {
                    isError = true;
                    Log.e(TAG, "<客户端" + bean.getRemark() + "接收" + address + "异常>\n" + e.getMessage() + "\n" + CrashHandler.getInstance().printCrash(e));
                }
                Log.i(TAG, "<客户端" + bean.getRemark() + "接收" + address + "消息结束>");
            } catch (IOException e) {
                isError = true;
                Log.e(TAG, "<客户端" + bean.getRemark() + "连接" + address + "异常>\n" + e.getMessage() + "\n" + CrashHandler.getInstance().printCrash(e));
            } finally {
                if (isError) {
                    if (retry < 10) {
                        Log.i(TAG, "<客户端" + bean.getRemark() + "连接" + address + "异常>，重试---" + retry);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException interruptedException) {
                        }
                        clientSend(address, bean, retry + 1);
                    }
                }
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            Log.i(TAG, "<客户端" + bean.getRemark() + "发送" + address + "结束>");
        });
    }

    public interface ClientReceive {
        void dealMessage(String message);
    }

    /**
     * 客户端发送消息
     *
     * @param address       连接地址
     * @param bean          消息内容
     * @param retry         重试次数
     * @param clientReceive 客户端处理消息的接口
     */
    public static void clientSend(String address, MessageBean bean, int retry, ClientReceive clientReceive) {

        String message = mGson.toJson(bean);
        ExecutorUtil.doExecute(() -> {
            Log.i(TAG, "<客户端" + bean.getRemark() + "发送开始>");
            mClient = new LocalSocket();
            InputStream is = null;
            OutputStream os = null;
            boolean isError = false;
            try {
                localSocketAddress = new LocalSocketAddress(address);
                mClient.connect(localSocketAddress);

                if (!mClient.isConnected()) {
                    Log.i(TAG, "<客户端" + bean.getRemark() + "连接" + address + "失败>");
                    return;
                }

                is = mClient.getInputStream();
                os = mClient.getOutputStream();

                Log.i(TAG, "<客户端" + bean.getRemark() + "发送" + address + "消息开始>" + message);
                os.write(message.getBytes());
                os.write(0);
                os.flush();
                Log.i(TAG, "<客户端" + bean.getRemark() + "发送" + address + "消息结束>");
                Log.i(TAG, "<客户端" + bean.getRemark() + "接收" + address + "消息开始>");
                try {
                    String resultStr = null;
                    int readed = is.read();
                    int size = 0;
                    byte[] bytes = new byte[0];
                    while (readed != -1) {
                        byte[] copy = new byte[10240];
                        System.arraycopy(bytes, 0, copy, 0, bytes.length);
                        bytes = copy;
                        bytes[size++] = (byte) readed;
                        //以换行符标识成结束
                        if ('\0' == (byte) readed) {
                            resultStr = new String(bytes, 0, size - 1);
                            break;
                        }
                        readed = is.read();
                    }
                    if (clientReceive != null && StringUtils.isNotBlank(resultStr)) {
                        clientReceive.dealMessage(resultStr);
                    }
                    Log.i(TAG, "<客户端" + bean.getRemark() + "接收到" + address + "服务器消息>" + resultStr);
                } catch (IOException e) {
                    isError = true;
                    Log.e(TAG, "<客户端" + bean.getRemark() + "接收" + address + "异常>\n" + e.getMessage() + "\n" + CrashHandler.getInstance().printCrash(e));
                }
                Log.i(TAG, "<客户端" + bean.getRemark() + "接收" + address + "消息结束>");
            } catch (IOException e) {
                isError = true;
                Log.e(TAG, "<客户端" + bean.getRemark() + "连接" + address + "异常>\n" + e.getMessage() + "\n" + CrashHandler.getInstance().printCrash(e));
            } finally {
                if (isError) {
                    if (retry < 10) {
                        Log.i(TAG, "<客户端" + bean.getRemark() + "连接" + address + "异常>，重试---" + retry);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException interruptedException) {
                        }
                        clientSend(address, bean, retry + 1);
                    }
                }
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            Log.i(TAG, "<客户端" + bean.getRemark() + "发送" + address + "结束>");
        });
    }

    /**
     * 服务端接收消息
     *
     * @param address 服务地址
     * @return
     */
    public static boolean startServer(String address, ConnectHandler handler) {
        try {
            mServer = new LocalServerSocket(address);
        } catch (IOException e) {
            return false;
        }
        setHandler(handler);
        ExecutorUtil.doExecuteForLocalSocket(() -> {
            for (; ; ) {
                Log.i(TAG, "<服务端" + address + "接收开始>");
                try {
                    LocalSocket accept = mServer.accept();

                    if (accept == null) {
                        Log.i(TAG, "<连接" + address + "失败>");

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                        }
                        continue;
                    }
                    Log.i(TAG, "<连接" + address + "成功>");
                    serverMessage(address, accept);
                } catch (Exception e) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e2) {
                    }
                    Log.e(TAG, "<连接" + address + "异常>\n" + e.getMessage() + "\n" + CrashHandler.getInstance().printCrash(e));
                }
                Log.i(TAG, "<服务端" + address + "接收结束>");
            }
        });
        return true;
    }

    /**
     * 服务端接收消息
     *
     * @param address
     * @param accept
     */
    private static void serverMessage(String address, LocalSocket accept) {
        ExecutorUtil.doExecute(() -> {
            InputStream is = null;
            OutputStream os = null;
            try {
                Log.i(TAG, "<服务端" + address + "接收消息开始>");
                //获取输入流,读取接收到的消息
                is = accept.getInputStream();
                os = accept.getOutputStream();

                //接收到的消息
                String resultStr = null;
                int readed = is.read();
                int size = 0;
                byte[] bytes = new byte[0];
                while (readed != -1) {
                    byte[] copy = new byte[10240];
                    System.arraycopy(bytes, 0, copy, 0, bytes.length);
                    bytes = copy;
                    bytes[size++] = (byte) readed;
                    //以结束符标识成结束
                    if ('\0' == (byte) readed) {
                        resultStr = new String(bytes, 0, size - 1);
                        Log.i(TAG, "<服务端" + address + "接收到了消息>" + resultStr);
                        break;
                    }
                    readed = is.read();
                }
                //设置消息内容
                decodeMessage(resultStr);

                Log.i(TAG, "<服务端" + address + "接收消息结束>");
                //获取输出流,往客户端发送消息
                Log.i(TAG, "<服务端" + address + "发送消息开始>");
                try {
                    if (StringUtils.isNotBlank(mHandler.getMessage())) {
                        os.write(mHandler.getMessage().getBytes());
                        mHandler.clearMessage();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "<服务端" + address + "往客户端发送消息失败>\n" + e.getMessage() + "\n" + CrashHandler.getInstance().printCrash(e));
                }
                os.write(0);
                os.flush();
                Log.i(TAG, "<服务端" + address + "发送消息结束>");
            } catch (Exception e) {
                Log.e(TAG, "<服务端" + address + "接收消息异常>\n" + e.getMessage() + "\n" + CrashHandler.getInstance().printCrash(e));
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * 解析消息
     *
     * @param resultStr
     */
    private static void decodeMessage(String resultStr) {
        try {
            JSONObject jsonObject = new JSONObject(resultStr);
            int type = jsonObject.getInt("type");
            mHandler.handleMessage(type, resultStr);
        } catch (Exception e) {
            Log.e(TAG, "<解析消息失败>\n" + e.getMessage() + "\n" + CrashHandler.getInstance().printCrash(e));
        }
    }

}
