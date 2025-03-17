package com.example.nettyclient.netty.message;

import java.nio.ByteBuffer;

public class ByteBufferMessage {
//    private long sessionId;
    private int cid;
    private int errorCode;
    private int protocolId;
    private ByteBuffer byteBuffer;

    public ByteBufferMessage() {
    }

    public ByteBufferMessage(int cid, int errorCode, int protocolId, ByteBuffer byteBuffer) {
        this.cid = cid;
        this.errorCode = errorCode;
        this.protocolId = protocolId;
        this.byteBuffer = byteBuffer;
    }

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public int getProtocolId() {
        return protocolId;
    }

    public void setProtocolId(int protocolId) {
        this.protocolId = protocolId;
    }

    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }

    public void setByteBuffer(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
    }
}
