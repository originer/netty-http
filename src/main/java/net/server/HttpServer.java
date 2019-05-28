package net.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import net.http.AbstractContentAdapter;
import net.http.AbstractControllerAdapter;
import net.http.HttpChannelInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpServer {

    private Logger logger = LoggerFactory.getLogger(HttpServer.class);
    private AbstractControllerAdapter adapter;
    private AbstractContentAdapter parser;

    public HttpServer(AbstractControllerAdapter adapter) {
        this.adapter = adapter;
    }

    public HttpServer(AbstractControllerAdapter adapter, AbstractContentAdapter parser) {
        this.adapter = adapter;
        this.parser = parser;
    }

    public void startup(int inetPort, boolean soKeepAlive) {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        (bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).
                childHandler(new HttpChannelInitializer(adapter, parser)).
                option(ChannelOption.SO_BACKLOG, Integer.valueOf(1024))).
                childOption(ChannelOption.SO_REUSEADDR, Boolean.TRUE).
                childOption(ChannelOption.TCP_NODELAY, Boolean.TRUE).
                childOption(ChannelOption.SO_KEEPALIVE, soKeepAlive);
        try {
            bootstrap.bind(inetPort);
        } catch (Exception ex) {
            throw new RuntimeException("The server fail to listen on port " + inetPort);
        }

        this.logger.info("Server startup, listening on {} ...", inetPort);
    }
}
