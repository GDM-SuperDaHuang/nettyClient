package com.netty.test;

import com.google.protobuf.ByteString;
import com.netty.message.DHKeyInfo;
import com.netty.message.MsgDecode;
import com.netty.message.Myhandle;
import com.slg.module.message.Constants;
import com.slg.module.message.ErrorCodeConstants;
import com.slg.module.message.MsgUtil;
import com.slg.module.util.CryptoUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.ReferenceCountUtil;
import message.Account;
import message.Friend;
import message.Login;

import javax.crypto.SecretKey;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Scanner;

public class TestClient {
    public Channel channel;
    int serverPort = 8100;

    DHKeyInfo dhKeyInfo = DHKeyInfo.getInstance();

    public void start() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler())
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
//                        p.addLast("log", new LoggingHandler(LogLevel.INFO));
                        p.addLast(new MsgDecode());
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
                    try {
                        sendMsg(protoId, next);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
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


    public void sendMsg(int protoId, String message) throws Exception {

        byte[] byteArray = null;
        byte zip = 0;
        byte encrypted = 0;
        ByteBuf buf = null;
        if (protoId == 4) {
            for (int i = 0; i < 1; i++) {
                Login.LoginReq loginRequest = Login.LoginReq.newBuilder()
                        .setAccount(message)
                        .setPwd("123456")
                        .buildPartial();
                byteArray = loginRequest.toByteArray();
                encrypted = 1;
                ByteBuf reqbody = Unpooled.wrappedBuffer(byteArray);


                SecretKey aesKey = CryptoUtils.generateAesKey(dhKeyInfo.getSharedKey());

                ByteBuf encryptedBuf = CryptoUtils.encrypt(aesKey, reqbody);

                //加密后的长度
                short encryptedLength = (short) encryptedBuf.readableBytes();
                ByteBuf byteBuf = buildClientMsgAndSend1(1, 0, protoId, zip, encrypted, encryptedBuf);
                // ... 构建消息的代码 ...
                channel.writeAndFlush(byteBuf).addListener(future -> {
                    encryptedBuf.release();
                    if (future.isSuccess()) {
                        System.out.println("客户端消息发送成功");
                    } else {
                        System.out.println("客户端消息发送失败: " + future.cause().getMessage());
                    }
                });

            }
        } else if (protoId == 2) {//密钥交换
            for (int i = 0; i < 1; i++) {
                // 生成DH参数
                SecureRandom random = new SecureRandom();
                BigInteger p = BigInteger.probablePrime(512, random);// 512位素数
                BigInteger g = new BigInteger("2");
                BigInteger privateKey = new BigInteger(p.bitLength() - 2, random).add(BigInteger.ONE);
                BigInteger publicKey = g.modPow(privateKey, p);
                Account.KeyExchangeReq friendRequest = Account.KeyExchangeReq.newBuilder()
                        .setG(ByteString.copyFrom(g.toByteArray()))
                        .setP(ByteString.copyFrom(p.toByteArray()))
                        .setPublicKey(ByteString.copyFrom(publicKey.toByteArray()))
                        .buildPartial();
                byteArray = friendRequest.toByteArray();
                dhKeyInfo.setP(p);
                dhKeyInfo.setPrivateKey(privateKey);
                buildClientMsgAndSend(1, 0, protoId, zip, encrypted, byteArray);
            }

        } else if (protoId == 3) {//密钥验证
            for (int i = 0; i < 10000; i++) {
                String test = "Hello, World!";
                Account.KeyVerificationReq verificationReq = Account.KeyVerificationReq.newBuilder()
                        .setTestMessage(ByteString.copyFrom(test.getBytes(StandardCharsets.UTF_8)))
                        .buildPartial();
                byteArray = verificationReq.toByteArray();
                ByteBuf plaintextBuf = Unpooled.wrappedBuffer(byteArray);


                // 生成DH参数
                SecretKey aesKey = CryptoUtils.generateAesKey(dhKeyInfo.getSharedKey());
                ByteBuf encryptedBuf = CryptoUtils.encrypt(aesKey, plaintextBuf);

                encrypted = 1;
                ByteBuf out = buildClientMsgAndSend1(1, 0, protoId, zip, encrypted, encryptedBuf);
                // ... 构建消息的代码 ...
                channel.writeAndFlush(out).addListener(future -> {
                    encryptedBuf.release();
                    plaintextBuf.release();
                    if (future.isSuccess()) {
                        System.out.println("客户端消息发送成功");
                    } else {
                        System.out.println("客户端消息发送失败: " + future.cause().getMessage());
                    }
                });
            }

        } else if (protoId == 101) {
            for (int i = 0; i < 1; i++) {
                Friend.FriendRequest friendRequest = Friend.FriendRequest.newBuilder()
                        .setUserId(Long.valueOf(message))
                        .buildPartial();
                byteArray = friendRequest.toByteArray();
                buildClientMsgAndSend(1, 0, protoId, zip, encrypted, byteArray);
            }

        }

    }

    public ByteBuf buildClientMsgAndSend1(int cid, int errorCode, int protocolId, byte zip, byte encrypted, ByteBuf body) {
        short length = (short) body.readableBytes(); // 这里获取长度

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
        return out;
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
//                System.out.println("客户端消息发送成功");
            } else {
//                System.out.println("客户端消息发送失败: " + future.cause().getMessage());
            }
        });
        return out;
    }

}
