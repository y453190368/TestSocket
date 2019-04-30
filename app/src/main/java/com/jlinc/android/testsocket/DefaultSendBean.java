package com.jlinc.android.testsocket;


import com.xuhao.didi.core.iocore.interfaces.ISendable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Created by didi on 2018/6/4.
 */

public class DefaultSendBean implements ISendable {
    protected String content = "";

    @Override
    public final byte[] parse() {
        short cmd1 = 0;
        byte[] body = content.getBytes(Charset.defaultCharset());//这是内容
        byte[] cmd = shortToByte(cmd1);//这是指令
        byte[] length = shortToByte((short)(cmd.length + body.length + 2));//这是总长度
        byte[] sss = concat(length, cmd);
        byte[] total = concat(sss, body);
        ByteBuffer bb = ByteBuffer.allocate(total.length);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put(total);
        return bb.array();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    /**
     * 注释：short到字节数组的转换！
     *
     * @param number
     * @return
     */
    public static byte[] shortToByte(short number) {
        int temp = number;
        byte[] b = new byte[2];
        for (int i = 0; i < b.length; i++) {
            b[i] = new Integer(temp & 0xff).byteValue();
            //将最低位保存在最低位
            temp = temp >> 8; // 向右移8位
        }
        return b;
    }

    public static <T> byte[] concat(byte[] first, byte[] second) {
        byte[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }


}
