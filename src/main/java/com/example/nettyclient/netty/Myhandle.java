package com.example.nettyclient.netty;

import com.example.nettyclient.netty.message.ByteBufferMessage;
import com.example.nettyclient.netty.pb.MSG;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.DecoderException;

public class Myhandle extends SimpleChannelInboundHandler<ByteBufferMessage> {
//    @Override
//    protected void channelRead0(ChannelHandlerContext channelHandlerContext, MSG.LoginResponse loginResponse) throws Exception {
//        System.out.println("------------收到数据::::--------------"+loginResponse);
//    }
@Override
protected void channelRead0(ChannelHandlerContext ctx, ByteBufferMessage msg) throws Exception {
    System.out.println("------------收到数据::::--------------"+msg);
    int protocolId = msg.getProtocolId();
    if (protocolId==1){
        MSG.LoginResponse loginResponse = MSG.LoginResponse.parseFrom(msg.getByteBuffer());
        System.out.println(loginResponse);
    }
    if (protocolId==2){
        MSG.FriendsResponse friendsResponse = MSG.FriendsResponse.parseFrom(msg.getByteBuffer());
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
        // 关闭连接，因为发生异常后通常无法继续正常通信
        ctx.close();
    }


}
