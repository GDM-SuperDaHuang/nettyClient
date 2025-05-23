package com.netty;


import com.netty.message.ByteBufferMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.DecoderException;
import message.Friend;
import message.Login;

import java.net.SocketException;

public class Myhandle extends SimpleChannelInboundHandler<ByteBufferMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBufferMessage msg) throws Exception {
        System.out.println("------------收到数据::::--------------" + msg);
        int protocolId = msg.getProtocolId();
        int errorCode = msg.getErrorCode();
        if (errorCode != 0) {
            System.out.println("------------错误码!!!--------------" + msg+"错误码:"+errorCode);
            return;
        }

        if (protocolId == 4) {
            Login.LoginResp loginResponse = Login.LoginResp.parseFrom(msg.getByteBuffer());
            System.out.println(loginResponse);
        }
        if (protocolId == 20) {
            Friend.FriendsResponse friendsResponse = Friend.FriendsResponse.parseFrom(msg.getByteBuffer());
            System.out.println(friendsResponse);
        }

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
