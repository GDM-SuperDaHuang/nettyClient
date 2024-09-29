package com.example.nettyclient.netty;

import io.grpc.netty.shaded.io.netty.buffer.ByteBuf;
import io.grpc.netty.shaded.io.netty.channel.ChannelHandlerContext;
import io.grpc.netty.shaded.io.netty.handler.codec.ByteToMessageDecoder;
import org.springframework.stereotype.Component;


import java.util.List;

/**
 * 注意确保之前的包完整性
 */
//@ChannelHandler.Sharable
@Component
public class MsgDecode extends ByteToMessageDecoder {
    private short length = 0;
    //入
    @Override
    protected void decode(ChannelHandlerContext cxt, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < 16) { // 确保有足够的字节来读取头部
            return;
        }
        long sessionId =0;
        int protocolId =0;
        byte zip = 0;
        byte pbVersion = 0;
        if (length == 0) {
            //消息头
            //sessionId
            sessionId = in.readLong();
            //消息id
            protocolId = in.readInt();
            // 压缩标志
            zip = in.readByte();
            //版本
            pbVersion = in.readByte();
            //长度
            length = in.readShort();
        }
        if (in.readableBytes() < length) {//长度不够继续等待
            return;
        }

        //消息体
        byte[] bytes = new byte[length];
        // 6. 读取protobuf字节数组
        in.readBytes(bytes, 0, length);
        out.add(bytes);
    }


}
