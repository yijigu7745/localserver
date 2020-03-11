package com.lkb.localserver;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.SystemClock;

import com.yijigu.localconnect.ExecutorUtil;
import com.yijigu.localconnect.localserver.ConnectHandler;
import com.yijigu.localconnect.localserver.LocalServerConnect;
import com.yijigu.localconnect.localserver.MessageBean;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ExecutorUtil.doExecute(this::test);
    }

    private void test() {
        LocalServerConnect.startServer("localServer", mHandler);
        SystemClock.sleep(5000);
        LocalServerConnect.clientSend("localServer",new MessageBean()
        .setCode(0)
        .setType(1)
        .setRemark("测试发送消息"), 0);
    }

    ConnectHandler mHandler = new ConnectHandler() {
        @Override
        public void handleMessage(int type, String message) {
            setMessage("服务器返回的值");
        }
    };
}
