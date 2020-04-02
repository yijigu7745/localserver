package com.lkb.localserver;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.yijigu.localconnect.localserver.ConnectHandler;
import com.yijigu.localconnect.localserver.LocalSocketServer;
import com.yijigu.localconnect.localserver.MessageBean;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    private Context mContext;

    Button button;
    Button button2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        initView();
        registerView();
        startServer();
    }

    private void startServer() {
        new LocalSocketServer(address -> {
            Log.i(TAG, "offline: " + address);
            Observable.just("start Client")
                    .delay(10, TimeUnit.SECONDS)
                    .subscribe(s -> {
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addCategory(Intent.CATEGORY_LAUNCHER);
                        ComponentName cn = new ComponentName("com.lkb.localclient",
                                "com.lkb.localclient.MainActivity");
                        intent.setComponent(cn);
                        if (intent.resolveActivityInfo(mContext.getPackageManager(),
                                PackageManager.MATCH_DEFAULT_ONLY) != null) {
                            //启动的intent存在
                            mContext.startActivity(intent);
                        } else {
                            Toast.makeText(mContext, "应用未安装", Toast.LENGTH_SHORT).show();
                        }
                    });
        }).startServer("testServer", mHandler);
    }

    private Gson gson = new Gson();
    private ConnectHandler mHandler = new ConnectHandler() {
        @Override
        public void handleMessage(int type, String message) {
            switch (type) {
                case 2:
                    break;
                case 998:
                    getBindConnect().serverSend("testClient",
                            new MessageBean().setContent("测试回传"));
                    break;
                case 1000:
                    Log.i(TAG, "handleMessage: " + message);
                    MessageBean messageBean = gson.fromJson(message, MessageBean.class);
                    Log.i(TAG, "handleMessage: " + gson.toJson(messageBean));
                    getBindConnect().serverSend(messageBean.getRemark(), messageBean);
                    break;
                    default:
            }
        }
    };

    private void registerView() {
        button2.setOnClickListener(this);
        button.setOnClickListener(this);
    }

    private void initView() {
        button = findViewById(R.id.btn_test);
        button2 = findViewById(R.id.btn_test_2);
        button.setText("发送消息");
        button2.setText("连接服务器");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_test:
                break;
            case R.id.btn_test_2:
                break;
                default:
        }
    }
}
