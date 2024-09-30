package com.example.nettyclient.netty;

import com.example.nettyclient.netty.pb.MSG;
//import io.grpc.netty.shaded.io.netty.buffer.ByteBuf;
//import io.grpc.netty.shaded.io.netty.channel.ChannelHandlerContext;
//import io.grpc.netty.shaded.io.netty.handler.codec.MessageToByteEncoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.springframework.stereotype.Component;

/**
 * 确保之前的包完整性
 */
@Component
public class MsgEncode extends MessageToByteEncoder<MSG.LoginRequest> {
    @Override
    protected void encode(ChannelHandlerContext ctx, MSG.LoginRequest msg, ByteBuf out) throws Exception {
        System.out.println("99999999999999999999999");
//        ByteBuf out = ctx.alloc().buffer();
        //8 字节session
        out.writeLong(1111111111L);
        //消息id
        out.writeInt(1);
        //压缩标志
        out.writeByte(0);
        //版本
        out.writeByte(3);
        byte[] byteArray = msg.toByteArray();
        int length = byteArray.length;
        out.writeShort(length);
        out.writeBytes(byteArray);
        ctx.writeAndFlush(out);
    }
}
