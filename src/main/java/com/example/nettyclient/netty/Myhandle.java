package com.example.nettyclient.netty;

import com.example.nettyclient.netty.pb.MSG;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class Myhandle extends SimpleChannelInboundHandler<MSG.LoginResponse> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, MSG.LoginResponse loginResponse) throws Exception {
        System.out.println("------------收到数据--------------"+loginResponse);
    }
}
