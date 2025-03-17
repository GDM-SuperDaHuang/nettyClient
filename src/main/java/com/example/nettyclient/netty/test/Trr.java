package com.example.nettyclient.netty.test;

import com.example.nettyclient.netty.MsgDecode;
import com.example.nettyclient.netty.MsgEncode;
import com.example.nettyclient.netty.Myhandle;
import com.example.nettyclient.netty.pb.MSG;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.Scanner;

public class Trr {
    Channel channel;
    int serverPort = 8888;

    public void start() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
//                        p.addLast("log", new LoggingHandler(LogLevel.INFO));
                        p.addLast(new MsgDecode());
                        p.addLast(new MsgEncode());
                        p.addLast(new Myhandle());
                    }
                });
        try {
            ChannelFuture f = b.connect("127.0.0.1", serverPort).sync();
            channel = f.channel();
            new Thread(() -> {
                Scanner scanner = new Scanner(System.in);
                while (true) {
                    System.out.println("请输入消息（输入'exit'退出）:");
                    int protoId = scanner.nextInt();
                    scanner.nextLine(); // 消耗换行符
                    String next = scanner.nextLine();
                    if ("exit".equals(next)) {
                        try {
                            channel.close().sync(); // 用户输入'exit'时关闭 Channel 并等待关闭完成
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        break; // 退出循环
                    }
                    sendMsg(protoId, next);
                }
                scanner.close(); // 关闭扫描器
            }).start();
            // 等待 Channel 关闭完成后再关闭 EventLoopGroup
            channel.closeFuture().sync();
        } finally {
            System.out.println("优雅地关闭");
            // 优雅地关闭 EventLoopGroup
            group.shutdownGracefully().sync();
        }
    }

    private void sendMsg(int protoId, String message) {
        ByteBuf buf = Unpooled.buffer();

        // 1. 8 字节session
        buf.writeInt(110);
        buf.writeInt(0);
        //消息id
        buf.writeInt(protoId);
        //压缩标志
        buf.writeByte(0);
        //版本
        buf.writeByte(3);
        byte[] byteArray = null;
        if (protoId == 1) {
            MSG.LoginRequest loginRequest = MSG.LoginRequest.newBuilder()
                    .setUsername("大号" + message)
                    .setPassword("123456" + message)
                    .buildPartial();
            byteArray = loginRequest.toByteArray();
        }else if (protoId == 2){
            MSG.FriendRequest friendRequest = MSG.FriendRequest.newBuilder()
                    .setUserId(1313L)
                    .buildPartial();
            byteArray = friendRequest.toByteArray();
        }


        int length = byteArray.length;
        buf.writeShort(length);
        //消息体
        buf.writeBytes(byteArray);

        // ... 构建消息的代码 ...
        channel.writeAndFlush(buf).addListener(future -> {
            if (future.isSuccess()) {
                System.out.println("客户端消息发送成功");
            } else {
                System.out.println("客户端消息发送失败: " + future.cause().getMessage());
            }
        });
    }

    public static void main(String[] args) throws Exception {
        new Trr().start();
    }
}
