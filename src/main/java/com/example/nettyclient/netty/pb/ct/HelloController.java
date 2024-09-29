package com.example.nettyclient.netty.pb.ct;

import com.example.nettyclient.netty.NetClient;
import com.example.nettyclient.netty.pb.MSG;
import io.grpc.netty.shaded.io.netty.buffer.ByteBuf;
import io.grpc.netty.shaded.io.netty.buffer.Unpooled;
import io.grpc.netty.shaded.io.netty.channel.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HelloController {
    @Autowired
    private NetClient netClient;
    @GetMapping("/hello")
    @ResponseBody
    public String helloPost() {
        ByteBuf buf = Unpooled.buffer();
        // 1. 8 字节session
        buf.writeLong(1111111111L);
        //消息id
        buf.writeInt(0);
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


        Channel channel = netClient.getChannel();
        boolean success = channel.writeAndFlush(buf).isSuccess();

        if (channel.isActive()) {
            // 发送数据到服务器
            channel.writeAndFlush(buf);
        }
        // 这里可以处理POST请求的逻辑
        // 例如，从请求体中读取数据，处理数据，然后返回响应
        return "Hello, this is a POST response!";
    }
}
