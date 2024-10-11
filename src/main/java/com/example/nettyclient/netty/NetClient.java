package com.example.nettyclient.netty;


import com.example.nettyclient.netty.pb.MSG;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.Scanner;

@Component
public class NetClient {

    public Channel channel;
    LoggingHandler loggingHandler = new LoggingHandler(LogLevel.INFO);


    public Channel getChannel() {
        return channel;
    }

    @PostConstruct
    public void init() throws Exception {
        System.out.println("初始化");
        start();
    }

    public void start() throws Exception {
        // 创建客户端的 EventLoopGroup
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        // 创建 Bootstrap 实例
        b.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast("log",loggingHandler);
                        p.addLast(new MsgDecode());
                        p.addLast(new MsgEncode());
                        p.addLast(new Myhandle());
                    }
                });
        try {
            // 连接到服务器并等待连接完成
            ChannelFuture f = b.connect("127.0.0.1", 8999).sync();
//            f.channel().closeFuture().sync();
            channel = f.channel();
            new Thread(()->{
                Scanner scanner = new Scanner(System.in);
                while (true){
                    if (f.isSuccess()){
                        if (channel.isActive()) {
                            System.out.println("000000");

                            String next = scanner.nextLine();
                            if (!next.equals("q")){
                                // 发送数据到服务器
                                System.out.println("000000发送数据到服务器");
                                ByteBuf buf = Unpooled.buffer();
                                // 1. 8 字节session
                                buf.writeLong(111222333);
                                //消息id
                                buf.writeInt(1);
                                //压缩标志
                                buf.writeByte(0);
                                //版本
                                buf.writeByte(3);
                                MSG.LoginRequest loginRequest = MSG.LoginRequest.newBuilder()
                                        .setUsername("大黄"+next)
                                        .setPassword("123456"+next)
                                        .buildPartial();

                                byte[] byteArray = loginRequest.toByteArray();
                                int length = byteArray.length;
                                buf.writeShort(length);
                                //消息体
                                buf.writeBytes(byteArray);
//                                ChannelFuture future = channel.write(buf);
                                ChannelFuture future = channel.writeAndFlush(buf);
                                future.addListener(new ChannelFutureListener() {
                                    @Override
                                    public void operationComplete(ChannelFuture future) throws Exception {
                                        if (future.isSuccess()) {
                                            System.out.println("客户端消息发送成功");
                                        } else {
                                            System.out.println("客户端消息发送失败: " + future.cause().getMessage());
                                        }
                                    }
                                });
                                System.out.println("999");
                            }else {
                                scanner.close();
                                // 如果输入了特定的命令（例如 "wqeq"），则关闭 Channel
                                channel.close();
                                break; // 退出循环
                            }

                        }
                    }
                }

            }).start();
            channel.closeFuture().sync();
//            channel.closeFuture();
        } finally {
            System.out.println("关闭: " );

            // 优雅地关闭
            group.shutdownGracefully().sync();
        }
    }

}
