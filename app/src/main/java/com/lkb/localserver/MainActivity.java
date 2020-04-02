package com.lkb.localserver;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.yijigu.localconnect.ExecutorUtil;
import com.yijigu.localconnect.localserver.LocalSocketClient;
import com.yijigu.localconnect.localserver.MessageBean;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    Button button;
    Button button2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        registerView();
//        startServer();
    }

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


    static LocalSocketClient connect;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_test:
                ExecutorUtil.doExecute(() -> {
                    sendMessage();
                });
                break;
            case R.id.btn_test_2:
                connect = new LocalSocketClient("testServer", "testClient", message -> {
                    Log.i(TAG, "onServerReply: " + message);
                }, () -> true);
                break;
        }
    }

    private void sendMessage() {

        if (connect != null) {
            connect.write(new MessageBean().setCode(998).setRemark("testClient"), new LocalSocketClient.WritingCallBack() {
                @Override
                public void onSuccess() {
                    Log.i(TAG,"onSuccess");
                }

                @Override
                public void onFailed(String message) {
                    Log.e(TAG,"onFailed" + message);
                }
            });
        }
    }
}
