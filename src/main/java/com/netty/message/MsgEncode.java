package com.netty.message;

import com.netty.message.ByteBufferMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.springframework.stereotype.Component;

/**
 * 确保之前的包完整性
 */
@Component
public class MsgEncode extends MessageToByteEncoder<ByteBufferMessage> {
    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBufferMessage msg, ByteBuf out) throws Exception {
        System.out.println("99999999999999999999999");

        // 写入消息头
//        out.writeLong(msg.getSessionId());      // 8字节
        out.writeInt(msg.getCid());      // 4字节
        out.writeInt(msg.getErrorCode());      // 4字节

        out.writeInt(msg.getProtocolId());      // 4字节
        out.writeByte(0);                       // zip压缩标志，1字节
        out.writeByte(1);                       // pb版本，1字节

        // 获取消息体长度并写入
        int length = msg.getByteBuffer().remaining();
        out.writeShort(length);                 // 消息体长度，2字节

        // 写入消息体
        out.writeBytes(msg.getByteBuffer());
    }
}
