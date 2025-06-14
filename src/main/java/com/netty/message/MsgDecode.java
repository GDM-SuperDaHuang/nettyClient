package com.netty.message;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * 注意确保之前的包完整性
 */
//@ChannelHandler.Sharable
public class MsgDecode extends ByteToMessageDecoder {

    //入
//    @Override
//    protected void decode(ChannelHandlerContext cxt, ByteBuf in, List<Object> out) throws Exception {
//        // 确保有足够的字节来读取头部
//        if (in.readableBytes() < 16) {
//            return;
//        }
//        // 缓存 readableBytes 不够则暂时到局部变量
//        int readableBytes = in.readableBytes();
//        // 消息头
////        long sessionId = in.readLong();
//        int cid = in.readInt();
//        int errorCode = in.readInt();
//        int protocolId = in.readInt();
//        byte zip = in.readByte();
//        byte encrypted = in.readByte();
//        short length = in.readShort();
//        // 检查是否有足够的字节来读取整个消息体
//        if (readableBytes < 16 + length) {
//            // 如果没有，丢弃已经读取的头部信息，并返回
//            in.readerIndex(in.readerIndex() - 16);
//            return;
//        }
//        // 零拷贝切片（引用计数+1）
//        ByteBuf body = in.readRetainedSlice(length);
//        out.add(new ByteBufferMessage(cid,errorCode,protocolId,zip,encrypted,length,body));
////        out.add(ByteBufferMessage.newInstance(cid, errorCode, protocolId, zip, encrypted, length, body));
//    }

    @Override
    protected void decode(ChannelHandlerContext cxt, ByteBuf in, List<Object> out) {
        // 头部固定 16 字节（4+4+4+1+1+2）
        if (in.readableBytes() < 16) {
            return;
        }

        // 标记当前 readerIndex，方便回退
        in.markReaderIndex();

        // 读取头部
        int cid = in.readInt();
        int errorCode = in.readInt();
        int protocolId = in.readInt();
        byte zip = in.readByte();
        byte encrypted = in.readByte();
        short length = in.readShort();

        // 检查消息体是否完整（实时检查）
        if (in.readableBytes() < length) {
            in.resetReaderIndex(); // 回退到头部开始位置
            return;
        }

        // 读取消息体（引用计数 +1）
        ByteBuf body = in.readRetainedSlice(length);
        out.add(new ByteBufferMessage(cid, errorCode, protocolId, zip, encrypted, length, body));
    }
}
