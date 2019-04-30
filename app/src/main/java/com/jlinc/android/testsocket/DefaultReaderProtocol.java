package com.jlinc.android.testsocket;

import com.xuhao.didi.core.protocol.IReaderProtocol;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 自定义解析
 */
public class DefaultReaderProtocol implements IReaderProtocol {
    @Override
    public int getHeaderLength() {
        return 2;
    }

    @Override
    public int getBodyLength(byte[] header, ByteOrder byteOrder) {
        if (header == null || header.length < getHeaderLength()) {
            return 0;
        }
        ByteBuffer bb = ByteBuffer.wrap(header);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getShort()-2;
    }
}
