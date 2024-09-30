package com.example.nettyclient.netty;


import com.example.nettyclient.netty.pb.MSG;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class NetClient {

    public Channel channel;

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
        try {
            // 创建 Bootstrap 实例
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new MsgDecode());
                            p.addLast(new Myhandle());
                            p.addLast(new MsgEncode());
                        }
                    });
            // 连接到服务器并等待连接完成
            ChannelFuture f = b.connect("127.0.0.1", 8888).sync();
//            f.channel().closeFuture().sync();
            if (f.isSuccess()){
                channel = f.channel();
                if (channel.isActive()) {
                    for (int i = 0; i < 10; i++) {
                        // 发送数据到服务器
                        System.out.println("发送数据到服务器");
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
                                .setUsername("大黄")
                                .setPassword("123213")
                                .buildPartial();

                        byte[] byteArray = loginRequest.toByteArray();
                        int length = byteArray.length;
                        buf.writeShort(length);
                        //消息体
                        buf.writeBytes(byteArray);
                        channel.writeAndFlush(buf);
                    }

                }
            }
            channel.closeFuture().sync();
        } finally {
            // 优雅地关闭
            group.shutdownGracefully().sync();
        }
    }

}
