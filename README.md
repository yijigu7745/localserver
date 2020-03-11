# localserver

Android本地文件通讯,可以跨进程跟跨APP

给LocalServerSocket封装了个启动服务跟发送消息的方法

先上依赖

  allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
  
  dependencies {
	        implementation 'com.github.yijigu7745:localserver:1.0'
	}
  
# 服务端:

  直接调用
  
    LocalServerConnect.startServer("test", mHandler);
  
  启动服务端,第一个参数为连接地址,第二个参数为接收到客户端消息后端的处理.
  
    private static ConnectHandler mHandler = new ConnectHandler() {
        @Override
        public void handleMessage(int type, String message) {
            setMessage("我是服务端");
        }
    };
    
   这是服务端接收到客户端消息后的响应.
   
   给客户端设置发消息:
   
    setMessage(String message);
    
# 客户端:
    
    给服务端发送消息
    
    LocalServerConnect.clientSend("test",
                        new MessageBean()
                                .setCode(1).setContent("我是客户端").setType(1),
                        0,
                        message -> {
                            LoggerUtil.logI(TAG, message);
                        });
     
    客户端与服务端不是保持着长连接,客户端发送完消息后就直接结束了.   
  
