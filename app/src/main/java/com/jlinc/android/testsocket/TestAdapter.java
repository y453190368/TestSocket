package com.jlinc.android.testsocket;

import android.util.Log;

import com.xuhao.didi.core.iocore.interfaces.IPulseSendable;
import com.xuhao.didi.core.iocore.interfaces.ISendable;
import com.xuhao.didi.core.pojo.OriginalData;
import com.xuhao.didi.socket.client.sdk.client.ConnectionInfo;
import com.xuhao.didi.socket.client.sdk.client.action.SocketActionAdapter;
import com.xuhao.didi.socket.client.sdk.client.connection.IConnectionManager;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.UUID;

public class TestAdapter extends SocketActionAdapter {
    IConnectionManager mManager;

    public TestAdapter(IConnectionManager mManager){
        this.mManager = mManager;
    }
    String password = "asdfwetyhjuytrfd";
    //    String message = "78AE92C8-71D9-695B-4445-F05A4D-10000";
    String uuid = UUID.randomUUID().toString().replaceAll("-", "");
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
