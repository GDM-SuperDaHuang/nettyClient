package com.netty.test;

import com.netty.message.MsgDecode;
import com.netty.message.MsgEncode;
import com.netty.Myhandle;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import message.Friend;
import message.Login;

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
                    System.out.println("请输入protoId,（输入'q'退出）:");
                    int protoId = scanner.nextInt();
                    scanner.nextLine(); // 消耗换行符
                    String next = scanner.nextLine();
                    if ("q".equals(next)) {
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
        byte[] byteArray = null;
        byte zip = 0;
        byte encrypted = 0;
        ByteBuf buf = null;
        if (protoId == 4) {
            for (int i = 0; i < 10000; i++) {
                Login.LoginReq loginRequest = Login.LoginReq.newBuilder()
                        .setAccount(message)
                        .setPwd("123456")
                        .buildPartial();
                byteArray = loginRequest.toByteArray();
                buildClientMsgAndSend(1, 0, protoId, zip, encrypted, byteArray);
            }
        } else if (protoId == 20) {
            Friend.FriendRequest friendRequest = Friend.FriendRequest.newBuilder()
                    .setUserId(Long.valueOf(message))
                    .buildPartial();
            byteArray = friendRequest.toByteArray();
            buildClientMsgAndSend(1, 0, protoId, zip, encrypted, byteArray);
        }

    }

    public ByteBuf buildClientMsgAndSend(int cid, int errorCode, int protocolId, byte zip, byte encrypted, byte[] body) {
        int length = body.length;
        //写回
        ByteBuf out = Unpooled.buffer(16 + length);
        //消息头
        out.writeInt(cid);      // 4字节
        out.writeInt(errorCode);   // 4字节
        out.writeInt(protocolId);  // 4字节
        out.writeByte(zip);         // zip压缩标志，1字节
        out.writeByte(encrypted);  // 加密标志，1字节
        //消息体
        out.writeShort(length);   // 消息体长度，2字节
        // 写入消息体
        if (body != null) {
            out.writeBytes(body);
        }

        // ... 构建消息的代码 ...
        channel.writeAndFlush(out).addListener(future -> {
            if (future.isSuccess()) {
                System.out.println("客户端消息发送成功");
            } else {
                System.out.println("客户端消息发送失败: " + future.cause().getMessage());
            }
        });
        return out;
    }

    public static void main(String[] args) throws Exception {
        new Trr().start();
    }
}
