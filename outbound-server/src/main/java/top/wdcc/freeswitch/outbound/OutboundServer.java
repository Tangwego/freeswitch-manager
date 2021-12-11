package top.wdcc.freeswitch.outbound;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.wdcc.freeswitch.common.EslEventDecoder;

public class OutboundServer {
    private static final Logger logger = LoggerFactory.getLogger(OutboundServer.class);

    private static final EventLoopGroup bossGroup = new NioEventLoopGroup();
    private static final EventLoopGroup workGroup = new NioEventLoopGroup();
    private final OutboundListener listener;

    public OutboundServer(OutboundListener outboundListener){
        this.listener = outboundListener;
    }

    public void start(int port) throws InterruptedException {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workGroup)
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .channelFactory(NioServerSocketChannel::new)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new StringEncoder())
//                                .addLast(new StringDecoder())
                                .addLast(new EslEventDecoder(true))
                                .addLast(new OutboundHandler(listener));
                    }
                });
        ChannelFuture future = serverBootstrap.bind(port).sync();
        Channel channel = future.channel();
        logger.debug("OutboundServer waiting for connections on port: {}", port);
        channel.closeFuture().sync();
    }

    public void stop(){
        workGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }

}
