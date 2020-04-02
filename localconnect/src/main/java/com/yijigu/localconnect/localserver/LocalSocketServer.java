package com.yijigu.localconnect.localserver;

import android.net.LocalServerSocket;
import android.net.LocalSocket;
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
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * @author Administrator
 */
public class LocalSocketServer {

    private static final String TAG = "LocalSocketServer";

    /**
     * 服务端
     */
    private LocalServerSocket mServer = null;

    private final Disposable mKeepAliveDisposable;

    private final Gson mGson = new Gson();

    private MessageBean<KeepAliveBean> mKeepAliveMessage = new MessageBean<>();

    private KeepAliveBean mKeepAliveBean = new KeepAliveBean();

    private HashMap<String, Long> replyTime = new HashMap<>();

    /**
     * 与服务端链接中的客户端Map
     */
    private volatile HashMap<String, LocalSocket> acceptList = new HashMap<>();

    private ConnectHandler mHandler = null;

    private final OffLineCallBack mOffLineCallBack;

    public interface OffLineCallBack {

        /**
         * 客户端掉线回调
         *
         * @param address
         */
        void onClientOffline(String address);
    }

    public LocalSocketServer(OffLineCallBack mOffLineCallBack) {
        this.mOffLineCallBack = mOffLineCallBack;
        this.mKeepAliveDisposable = keepAliveCheck();
    }

    /**
     * 每分钟的定时器
     *
     * @return
     */
    private Disposable initMinuteTask(Consumer<Long> consumer) {
        return initTimeTask(1, TimeUnit.MINUTES, consumer);
    }


    /**
     * 创建一个定时器
     *
     * @return
     */
    public static Disposable createIntervalTask(int period, TimeUnit timeUnit, Consumer<Long> consumer) {
        return initTimeTask(period, timeUnit, consumer);
    }

    /**
     * 定时器
     *
     * @param period
     * @param timeUnit
     * @param consumer
     * @return
     */
    private static Disposable initTimeTask(int period, TimeUnit timeUnit, Consumer<Long> consumer) {
        return Observable.interval(period, timeUnit)
                .subscribe(consumer);
    }

    /**
     * 保活定时器
     *
     * @return
     */
    private Disposable keepAliveCheck() {
        return Observable.interval(5, TimeUnit.SECONDS)
                .subscribe(aLong -> {
                    Log.i(TAG, "keepAliveCheck: " + aLong);
                    if (acceptList.size() > 0) {
                        for (String clientAddress : acceptList.keySet()) {
                            if (acceptList.get(clientAddress) != null) {
                                long nanoTime = System.currentTimeMillis();
                                serverSend(clientAddress, mKeepAliveMessage
                                        .setType(MessageType.HEART_BEAT)
                                        .setMissionInfo(mKeepAliveBean
                                                .setCount(aLong)
                                                .setPreTime(nanoTime)
                                                .setNowTime(nanoTime)));
                            }
                        }
                    }
                });
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
        this.mHandler = handler;
        mHandler.bind(this);
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
                    serverReceive(address, accept);
                } catch (Exception e) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e2) {
                    }
                    Log.e(TAG, "<建立连接" + address + "异常>\n"
                            + e.getMessage());
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
    public boolean serverSend(String address, MessageBean bean) {
        LocalSocket accept = acceptList.get(address);
        if (accept == null) {
            acceptList.remove(address);
            Log.e(TAG, "当前地址没有连接中的客户端");
            return false;
        }
        String message = mGson.toJson(bean);
        try {
            //获取输出流,往客户端发送消息
            OutputStream os = accept.getOutputStream();
            OutputStreamWriter osr = new OutputStreamWriter(os);
            BufferedWriter bw = new BufferedWriter(osr);
            //获取输出流,往客户端发送消息
            Log.i(TAG, "<往客户端" + address + "-" + accept.getFileDescriptor()
                    + "发送消息开始>");
            if (StringUtils.isNotBlank(message)) {
                bw.write(message);
                bw.write("\n");
                bw.flush();
                Log.i(TAG, "<往客户端" + address + "-" + accept.getFileDescriptor()
                        + "发送了消息>" + message);
            }
            Log.i(TAG, "<往客户端" + address + "-" + accept.getFileDescriptor()
                    + "发送消息结束>");
        } catch (Exception e) {
            Log.e(TAG, "<往客户端" + address + "-" + accept.getFileDescriptor()
                    + "发送消息失败>\n"
                    + e.getMessage());
            return false;
        }
        return true;
    }

    private int readNullCount = 0;

    /**
     * 服务端接收客户端消息
     *
     * @param address
     */
    private void serverReceive(String address, LocalSocket accept) {
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
                        JSONObject jsonObject = new JSONObject(resultStr);
                        int type = jsonObject.getInt("type");
                        String remark = jsonObject.getString("remark");

                        Log.i(TAG, "<服务端" + address + "接收到了" + accept.getFileDescriptor() + "的消息>" + resultStr);

                        if (type == MessageType.HEART_BEAT) {
                            replyTime.put(remark, System.currentTimeMillis());
                            continue;
                        }
                        //设置消息内容
                        decodeMessage(resultStr, accept);
                    } else {
                        readNullCount++;
                        if (readNullCount % 20 == 0) {
                            if (checkClientVerifyTime()) {
                                break;
                            }
                        }
                    }
                }
                Log.i(TAG, "客户端" + address + "已掉线,取消该连接读写监听");
            } catch (Exception e) {
                Log.e(TAG, "<服务端" + address + "接收消息异常>\n"
                        + e.getMessage());
            }
        });
    }

    /**
     * 客户端长时间没有回复，认为客户端掉线
     *
     * @return
     */
    private boolean checkClientVerifyTime() {
        for (String address : replyTime.keySet()) {
            try {
                long clientReplyTime = replyTime.get(address);
                if (System.currentTimeMillis() - clientReplyTime > 3000) {
                    Log.i(TAG, "checkClientVerifyTime: " + address + " reply time out !");
                    acceptList.remove(address);
                    Log.i(TAG, "acceptList: " + acceptList.keySet());
                    mOffLineCallBack.onClientOffline(address);
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析消息
     *
     * @param resultStr
     */
    private void decodeMessage(String resultStr, LocalSocket accept) {
        try {
            JSONObject jsonObject = new JSONObject(resultStr);
            int type = jsonObject.getInt("type");
            String remark = jsonObject.getString("remark");

            acceptList.put(remark, accept);
            if (mHandler != null) {
                mHandler.handleMessage(type, resultStr);
            } else {
                Log.e(TAG, "处理消息失败, handler为空");
            }
        } catch (Exception e) {
            Log.e(TAG, "<解析消息失败>\n"
                    + e.getMessage());
        }
    }
}
