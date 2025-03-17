package com.example.nettyclient.netty.message;

import com.google.protobuf.GeneratedMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;


@Component
public class SendMsg {
    public void send(ChannelHandlerContext ctx, byte[] msg) {
        ByteBuf buf = Unpooled.buffer(16);
        //消息头
        buf.writeLong(0);
        buf.writeInt(0);
        buf.writeByte(0);
        buf.writeByte(0);
        int length = msg.length;
        buf.writeShort(length);
        buf.writeBytes(msg);
        Channel channel = ctx.channel();
//        if (channel.isActive()){
//            System.out.println("channel 活跃");
//        }else {
//            System.out.println("channel fasle" +channel);
//        }
        ChannelFuture future = ctx.writeAndFlush(buf);
//        future.addListener(new ChannelFutureListener() {
//            @Override
//            public void operationComplete(ChannelFuture future) throws Exception {
//                if (future.isSuccess()) {
//                    System.out.println("消息发送成功");
//                } else {
//                    System.out.println("消息发送失败: " + future.cause().getMessage());
//                }
//            }
//        });

    }

    //  com.google.protobuf.GeneratedMessage.Builder<Builder>
    public void send(ChannelHandlerContext ctx, GeneratedMessage.Builder<?> builder) {
        byte[] msg = builder.buildPartial().toByteArray();
        ByteBuf buf = Unpooled.buffer(16);
        //消息头
        buf.writeLong(0);
        buf.writeInt(0);
        buf.writeByte(0);
        buf.writeByte(0);
        int length = msg.length;
        buf.writeShort(length);
        buf.writeBytes(msg);
        ChannelFuture future = ctx.writeAndFlush(buf);
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
//                    System.out.println("消息发送成功");
                } else {
//                    System.out.println("消息发送失败: " + future.cause().getMessage());
                }
            }
        });

    }

}
