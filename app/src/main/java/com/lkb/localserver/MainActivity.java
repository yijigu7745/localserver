package com.lkb.localserver;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;

import com.yijigu.localconnect.ExecutorUtil;
import com.yijigu.localconnect.localserver.ConnectHandler;
import com.yijigu.localconnect.localserver.LocalServerConnect;
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
        ExecutorUtil.doExecute(this::test);
    }

    private void registerView() {
        button2.setOnClickListener(this);
        button.setOnClickListener(this);
    }

    private void initView() {
        button = findViewById(R.id.btn_test);
        button2 = findViewById(R.id.btn_test_2);
    }

    static LocalServerConnect connect;
    private void test() {
        LocalServerConnect.getInstance().startServer("localServer", mHandler);
        SystemClock.sleep(5000);
        connect = LocalServerConnect.getInstance().initClient("localServer");
    }

    ConnectHandler mHandler = new ConnectHandler() {
        @Override
        public void handleMessage(int type, String message) {
            if(type % 3 == 0){
                setMessage("服务器发送的消息---");
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_test:
                ExecutorUtil.doExecute(()->{
                    sendMessage();
                });
                break;
            case R.id.btn_test_2:
                ExecutorUtil.doExecute(()->{
                });
                break;
        }
    }

    private int i = 0;

    private void sendMessage() {
        if(connect != null){
            connect.send("localServer",new MessageBean()
                    .setCode(0)
                    .setType(++i)
                    .setRemark("测试发送消息" + i));
        }
    }
}
