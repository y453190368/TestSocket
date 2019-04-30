package com.jlinc.android.testsocket;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.xuhao.didi.core.iocore.interfaces.IPulseSendable;
import com.xuhao.didi.core.iocore.interfaces.ISendable;
import com.xuhao.didi.core.pojo.OriginalData;
import com.xuhao.didi.socket.client.sdk.OkSocket;
import com.xuhao.didi.socket.client.sdk.client.ConnectionInfo;
import com.xuhao.didi.socket.client.sdk.client.OkSocketFactory;
import com.xuhao.didi.socket.client.sdk.client.OkSocketOptions;
import com.xuhao.didi.socket.client.sdk.client.action.SocketActionAdapter;
import com.xuhao.didi.socket.client.sdk.client.connection.IConnectionManager;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity{
    IConnectionManager mManager;
    
    final String password = "asdfwetyhjuytrfd";
    //    String message = "78AE92C8-71D9-695B-4445-F05A4D-10000";
    final String uuid = UUID.randomUUID().toString().replaceAll("-", "");
    SocketActionAdapter actionAdapter = new SocketActionAdapter() {
        @Override
        public void onSocketConnectionSuccess(ConnectionInfo info, String action) {
            String encryptedMsg = AESUtils.encrypt(uuid, password);
            Log.i("MainActivity", "onSocketConnectionSuccess:" + encryptedMsg);
            mManager.send(new HandShakeBean(encryptedMsg));
            mManager.getPulseManager().setPulseSendable(new PulseBean());
        }

        @Override
        public void onSocketDisconnection(ConnectionInfo info, String action, Exception e) {
            Log.i("MainActivity", e.getMessage());
            mManager.connect();

        }

        @Override
        public void onSocketConnectionFailed(ConnectionInfo info, String action, Exception e) {
            Log.i("MainActivity", e.getMessage());
//            mManager.connect();
        }

        @Override
        public void onSocketReadResponse(ConnectionInfo info, String action, OriginalData data) {

            int cmd = toInt(subBytes(data.getBodyBytes(), 0, 2));
            if (cmd == 1) {
                Log.i("MainActivity", "onSocketReadResponse:收到握手返回数据");
            } else if (cmd == 3) {
                Log.i("MainActivity", "onSocketReadResponse:收到心跳,喂狗成功");
                mManager.getPulseManager().feed();
            } else if (cmd == 5) {
                Log.i("MainActivity", "onSocketReadResponse:收到数据，开始执行操作");
                byte[] content = subBytes(data.getBodyBytes(), 2, data.getBodyBytes().length - 2);
                String str = new String(content, Charset.forName("utf-8"));
                Log.i("MainActivity5", str);
            }

        }

        @Override
        public void onSocketWriteResponse(ConnectionInfo info, String action, ISendable data) {
            byte[] bytes = data.parse();
            bytes = Arrays.copyOfRange(bytes, 0, bytes.length);
            String str = new String(bytes, Charset.forName("utf-8"));
            Log.i("MainActivity", "onSocketWriteResponse:" + str);
            mManager.getPulseManager().pulse();
        }

        @Override
        public void onPulseSend(ConnectionInfo info, IPulseSendable data) {
            byte[] bytes = data.parse();
            bytes = Arrays.copyOfRange(bytes, 0, bytes.length);
            String str = new String(bytes, Charset.forName("utf-8"));
            Log.i("MainActivity", "onPulseSend:发送心跳包(Heartbeat Sending)" + str);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
    }

    private void initData() {
        //创建线程池，模拟100个客户端请求服务压力
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(100);
        for (int i = 0;i<100;i++){
            final int index = i;
            fixedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    String threadName = Thread.currentThread().getName();
                    Log.v("ThreadTest", "线程："+threadName+",正在执行第"+index+"个任务");
                    IConnectionManager mManager;

                    ConnectionInfo mInfo;
                    mInfo = new ConnectionInfo("192.168.1.22", 4567);
//        mInfo = new ConnectionInfo("192.168.0.21", 9999);
                    OkSocketOptions.setIsDebug(true);
                    OkSocketOptions.Builder builder = new OkSocketOptions.Builder();
                    builder.setPulseFrequency(10000)
                    //绑定固定端口号
//        builder.setSocketFactory(new OkSocketFactory() {
//            @Override
//            public Socket createSocket(ConnectionInfo connectionInfo, OkSocketOptions okSocketOptions) throws Exception {
//                Random random=new Random();	 //使用Random函数产生随机数；
//                int a=random.nextInt(1025)+60000;
//                Socket socket = new Socket();
//                SocketAddress socketAddress = new InetSocketAddress(a);
//                socket.bind(socketAddress);
//                return socket;
//            }
//        });
                    .setReaderProtocol(new DefaultReaderProtocol());

                    mManager = OkSocket.open(mInfo).option(builder.build());
                    mManager.registerReceiver(new TestAdapter(mManager));
                    mManager.connect();
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mManager != null) {
            mManager.disconnect();
            mManager.unRegisterReceiver(actionAdapter);
        }
    }

    //截取byte数组
    public static byte[] subBytes(byte[] src, int begin, int count) {
        byte[] bs = new byte[count];
        System.arraycopy(src, begin, bs, 0, count);
        return bs;
    }

    //byte数组转int
    public static int toInt(byte[] bRefArr) {
        int iOutcome = 0;
        byte bLoop;

        for (int i = 0; i < bRefArr.length; i++) {
            bLoop = bRefArr[i];
            iOutcome += (bLoop & 0xFF) << (8 * i);
        }
        return iOutcome;
    }

}