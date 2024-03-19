package com.yu.rpc.protocol;

public class ProtocolMessage {
    /**
     * 请求头
     */
    private Header header;

    /**
     * 消息体
     */
    private T body;

    public static class Header{
        private byte magic;
        private byte version;
        private byte serializer;
        private byte type;
        private byte status;
        private byte requestId;
        private byte bodyLength;
    }
}
