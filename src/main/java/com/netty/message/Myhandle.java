package com.netty.message;

import com.slg.module.util.LZ4Compression;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.DecoderException;
import message.Account;
import message.Friend;
import message.Login;

import java.math.BigInteger;
import java.net.SocketException;
import java.nio.ByteBuffer;

public class Myhandle extends SimpleChannelInboundHandler<ByteBufferMessage> {
    DHKeyInfo dhKeyInfo = DHKeyInfo.getInstance();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBufferMessage msg) throws Exception {
        System.out.println("------------收到数据::::--------------" + msg);
        int protocolId = msg.getProtocolId();
        int errorCode = msg.getErrorCode();
        if (errorCode != 0) {
            msg.getBody().release();
            System.out.println("------------错误码!!!--------------" + msg + "错误码:" + errorCode);
            return;
        }
        if (protocolId == 2) {
            ByteBuf byteBuf;
            if (msg.getZip() == 1) {
                short length = msg.getBody().readShort();
                byteBuf = LZ4Compression.decompressWithLengthHeader(msg.getBody(), length);
            } else {
                byteBuf = msg.getBody();
            }


            BigInteger privateKey = dhKeyInfo.getPrivateKey();
            BigInteger p = dhKeyInfo.getP();
            ByteBuffer byteBuffer = byteBuf.nioBuffer();
            Account.KeyExchangeResp resp = Account.KeyExchangeResp.parseFrom(byteBuffer);
            BigInteger serverPublicKey = new BigInteger(resp.getPublicKey().toByteArray());
            dhKeyInfo.setPublicKey(serverPublicKey);
            BigInteger sharedKey = serverPublicKey.modPow(privateKey, p);
            dhKeyInfo.setSharedKey(sharedKey);
            System.out.println(resp);
        }
        if (protocolId == 4) {
            ByteBuf byteBuf;
            if (msg.getZip() == 1) {
                short length = msg.getBody().readShort();
                byteBuf = LZ4Compression.decompressWithLengthHeader(msg.getBody(), length);
                Login.LoginResp loginResponse = Login.LoginResp.parseFrom(byteBuf.nioBuffer());
                System.out.println(loginResponse);
                byteBuf.release();
            } else {
                byteBuf = msg.getBody();
                byteBuf = LZ4Compression.decompressWithLengthHeader(msg.getBody(), msg.getLength());
                Login.LoginResp loginResponse = Login.LoginResp.parseFrom(byteBuf.nioBuffer());
                System.out.println(loginResponse);
                byteBuf.release();
            }
        }
        if (protocolId == 20) {
            Friend.FriendsResponse friendsResponse = Friend.FriendsResponse.parseFrom(msg.getBody().nioBuffer());
            System.out.println(friendsResponse);
        }
        msg.getBody().release();

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.err.println("Decoder error: " + cause.getMessage());
        // 处理在 channelRead0 或其他方法中抛出的异常
        if (cause instanceof DecoderException) {
            System.err.println("Decoder error: " + cause.getMessage());
        } else {
            cause.printStackTrace();
        }
        if (cause instanceof SocketException) {
            // 关闭连接，因为发生异常后通常无法继续正常通信
            ctx.close();
        }

    }


}
