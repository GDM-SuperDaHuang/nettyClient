package com.netty.message;

import io.netty.buffer.ByteBuf;

/**
 * 客户端与服务器之间协议
 */
public class ByteBufferMessage {
    private int cid;//顺序号
    private int errorCode;//错误码
    private int protocolId;//协议id
    private byte zip;
    private byte encrypted;
    private short length;//长度
    //private ByteBuffer body;//消息体
    //private byte[] body;//消息体
    private ByteBuf body; // 改用 ByteBuf 避免拷贝

    public ByteBufferMessage(int cid, int errorCode, int protocolId, byte zip, byte encrypted, short length, ByteBuf body) {
        this.cid = cid;
        this.errorCode = errorCode;
        this.protocolId = protocolId;
        this.zip = zip;
        this.encrypted = encrypted;
        this.length = length;
        this.body = body;
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

    public byte getZip() {
        return zip;
    }

    public void setZip(byte zip) {
        this.zip = zip;
    }

    public byte getEncrypted() {
        return encrypted;
    }

    public void setEncrypted(byte encrypted) {
        this.encrypted = encrypted;
    }

    public short getLength() {
        return length;
    }

    public void setLength(short length) {
        this.length = length;
    }

    public ByteBuf getBody() {
        return body;
    }

    public void setBody(ByteBuf body) {
        this.body = body;
    }
}
