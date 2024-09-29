package com.example.nettyclient.netty;

import com.example.nettyclient.netty.pb.MSG;
import io.grpc.netty.shaded.io.netty.channel.ChannelHandlerContext;
import io.grpc.netty.shaded.io.netty.channel.SimpleChannelInboundHandler;

public class Myhandle extends SimpleChannelInboundHandler<MSG.LoginResponse> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, MSG.LoginResponse loginResponse) throws Exception {
        System.out.println("--------------------------");
    }
}
