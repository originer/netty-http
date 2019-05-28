package net.http;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

public class HttpChannelInitializer extends ChannelInitializer<SocketChannel> {
    private AbstractControllerAdapter adapter;
    private AbstractContentAdapter parser;

    public HttpChannelInitializer(AbstractControllerAdapter adapter) {
        this.adapter = adapter;
    }

    public HttpChannelInitializer(AbstractControllerAdapter adapter, AbstractContentAdapter parser) {
        this.adapter = adapter;
        this.parser = parser;
    }

    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipe = ch.pipeline();
        pipe.addLast("decoder", new HttpRequestDecoder())
                .addLast("encoder", new HttpResponseEncoder())
                .addLast("handler", new HttpChannelInboundHandler(adapter, parser));
    }
}