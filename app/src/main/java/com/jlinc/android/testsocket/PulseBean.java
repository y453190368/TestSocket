package com.jlinc.android.testsocket;


import com.xuhao.didi.core.iocore.interfaces.IPulseSendable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class PulseBean implements IPulseSendable {

    public PulseBean() {

    }

    @Override
    public byte[] parse() {
        short cmd1 = 2;
        byte[] cmd = shortToByte(cmd1);//这是指令
        byte[] length = shortToByte((short)(cmd.length + 2));//这是总长度
        byte[] sss = concat(length,cmd);
        ByteBuffer bb = ByteBuffer.allocate(sss.length);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put(sss);
        return bb.array();
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