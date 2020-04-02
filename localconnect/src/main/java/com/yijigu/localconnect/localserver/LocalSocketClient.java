package com.yijigu.localconnect.localserver;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.google.gson.Gson;
import com.yijigu.localconnect.ExecutorUtil;
import com.yijigu.localconnect.StringUtils;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * @author Administrator
 */
public class LocalSocketClient {

    private static final String TAG = "LocalSocketClient";

    private static final Gson gson = new Gson();

    public interface ErrorCallBack {
        /**
         * 错误回调
         *
         * @return
         */
        boolean onError();
    }

    /**
     * 读取服务器消息回调接口
     */
    public interface ReadingCallBack {

        /**
         * 读取成功回调
         *
         * @param message
         */
        void onReadSuccess(String message);
    }

    /**
     * 往服务器发送消息回调接口
     */
    public interface WritingCallBack {

        /**
         * 发送成功回调
         */
        void onSuccess();

        /**
         * 发送失败回调
         *
         * @param message
         */
        void onFailed(String message);
    }

    private final String mAddress;
    private final String mRemark;
    private final ReadingCallBack mReadingCallBack;
    private final ErrorCallBack mErrorCallBack;

    private final HandlerThread mWriterThread;
    private final Handler mWriterHandler;

    private final Object mLock = new Object();
    private LocalSocket mSocket;
    private boolean mClosed = false;

    public LocalSocketClient(String mAddress, String mRemark, ReadingCallBack mReadingCallBack, ErrorCallBack mErrorCallBack) {
        this.mAddress = mAddress;
        this.mRemark = mRemark;
        this.mReadingCallBack = mReadingCallBack;
        this.mErrorCallBack = mErrorCallBack;

        mWriterThread = new HandlerThread("local-socker-writer");
        mWriterThread.start();
        mWriterHandler = new Handler(mWriterThread.getLooper());
        mWriterHandler.post(this::initLocalSocket);
    }

    public void write(MessageBean messageBean, WritingCallBack callBack) {

        String message = gson.toJson(messageBean);

        mWriterHandler.post(() -> {
            LocalSocket socket = getSocket();
            if (socket == null) {
                initLocalSocket();
                socket = getSocket();
                if (socket == null) {
                    if (!closed()) {
                        callBack.onFailed(message);
                    }
                    return;
                }
            }

            try {
                OutputStream os = socket.getOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(os);
                BufferedWriter bw = new BufferedWriter(osw);

                Log.i(TAG, "<客户端发送消息开始>" + message);
                if (StringUtils.isNotBlank(message)) {
                    bw.write(message);
                    bw.write("\n");
                    bw.flush();
                }
                Log.i(TAG, "<客户端发送消息结束>" + message);
                callBack.onSuccess();
            } catch (Exception e) {
                Log.e(TAG, "<write>", e);
                closeSocket();
                callBack.onFailed(message);
                if (closed() && mErrorCallBack.onError()) {
                    initLocalSocket();
                }
            }
        });
    }

    private void initLocalSocket() {

        try {
            if (mSocket != null && !mSocket.isConnected()) {

            } else {
                LocalSocket socket = new LocalSocket();
                synchronized (mLock) {
                    if (mClosed) {
                        silentlyClose(socket);
                        return;
                    }
                    socket.connect(new LocalSocketAddress(mAddress));
                    mSocket = socket;
                    ExecutorUtil.doExecute(() -> new ReadTask(socket).run());
                    firstConnect(mAddress, mRemark);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "<initLocalSocket>", e);
        }
    }

    /**
     * 客户端第一次连接服务端,需要告知服务端,当前是哪个客户端连接的
     *
     * @param address
     * @param remark
     * @throws IOException
     */
    public void firstConnect(String address, String remark) {
        MessageBean bean = new MessageBean();
        bean.setType(MessageType.FIRST_CONNECT).setRemark(remark);
        String message = gson.toJson(bean);
        OutputStream os;
        OutputStreamWriter osw;
        BufferedWriter bw;
        try {
            os = mSocket.getOutputStream();
            osw = new OutputStreamWriter(os);
            bw = new BufferedWriter(osw);

            Log.i(TAG, "<客户端" + mSocket.getFileDescriptor()
                    + "发送" + address + "消息开始>" + message);
            if (StringUtils.isNotBlank(message)) {
                bw.write(message);
                bw.write("\n");
                bw.flush();
            }
            Log.i(TAG, "<客户端" + mSocket.getFileDescriptor()
                    + "发送" + address + "消息结束>");
        } catch (IOException e) {
            Log.e(TAG, "<客户端" + mSocket.getFileDescriptor() + "收发消息异常>\n"
                    + e.getMessage());
        }
    }

    private boolean closed() {
        synchronized (mLock) {
            return mClosed;
        }
    }

    private LocalSocket getSocket() {
        synchronized (mLock) {
            return mSocket;
        }
    }

    private void closeSocketLocked() {
        if (mSocket == null) {
            return;
        }

        silentlyClose(mSocket);
        mSocket = null;
    }

    private class ReadTask implements Runnable {

        private final LocalSocket mLocalSocket;

        public ReadTask(LocalSocket mLocalSocket) {
            this.mLocalSocket = mLocalSocket;
        }

        @Override
        public void run() {
            try {
                readResponse();
            } catch (Exception e) {
                Log.e(TAG, "ReaderTask#run: ", e);
            }
        }

        private void readResponse() throws IOException {
            if (mLocalSocket.isConnected()) {
                InputStream is;
                InputStreamReader isr;
                BufferedReader br;
                is = mLocalSocket.getInputStream();
                isr = new InputStreamReader(is);
                br = new BufferedReader(isr);
                while (true) {
                    String resultStr = br.readLine();
                    //以换行符标识成结束
                    if (StringUtils.isNotBlank(resultStr)) {
                        try {
                            JSONObject jsonObject = new JSONObject(resultStr);
                            int type = jsonObject.getInt("type");

                            if (type == MessageType.HEART_BEAT) {
                                write(new MessageBean().setRemark(mRemark).setType(MessageType.HEART_BEAT),
                                        new WritingCallBack() {

                                            @Override
                                            public void onSuccess() {

                                            }

                                            @Override
                                            public void onFailed(String message) {

                                            }
                                        });
                                continue;
                            }
                        } catch (Exception e) {
                        }
                        Log.i(TAG, "<客户端" + mLocalSocket.getFileDescriptor()
                                + "接收到服务端消息>" + resultStr);
                        mReadingCallBack.onReadSuccess(resultStr);
                    }
                }
            }
        }
    }

    private static void silentlyClose(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                Log.e(TAG, "silentlyClose: ", e);
            }
        }
    }

    private void closeSocket() {
        synchronized (mLock) {
            mClosed = true;
            closeSocketLocked();
        }
    }


}
