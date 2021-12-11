package top.wdcc.freeswitch.eslclient;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.wdcc.freeswitch.common.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractEslClient implements EslListener {
    private static final Logger logger = LoggerFactory.getLogger(AbstractEslClient.class);
    private static final EventLoopGroup group = new NioEventLoopGroup();

    private AtomicBoolean authenticatorResponded = new AtomicBoolean( false );
    private boolean authenticated = false;
    private Channel channel = null;

    /**
     * 连接到FreeSWITCH服务器
     * @param host   FreeSWITCH's esl host
     * @param port   FreeSWITCH's esl port
     * @param passwd FreeSWITCH's esl password
     * @param timeout connection timeout
     * @throws  EslConnectException, EslAuthenticateException, InterruptedException 3 Exceptions
     */
    public void connect(String host, int port, final String passwd, int timeout) throws EslConnectException, EslAuthenticateException, InterruptedException {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .option(ChannelOption.TCP_NODELAY, true)
                .channelFactory(NioSocketChannel::new)
                .handler(new ChannelInitializer<SocketChannel>() {
                    protected void initChannel(SocketChannel socketChannel){
                        socketChannel.pipeline()
                                .addLast(new StringEncoder())
                                .addLast(new EslEventDecoder())
                                .addLast(new EslClientHandler(passwd, AbstractEslClient.this));
                    }
                });
        ChannelFuture future = bootstrap.connect(host, port).sync();

        if (!future.awaitUninterruptibly(timeout)){
            throw new EslConnectException("Connect to FreeSWITCH: [ " + host + ":" + port + "] timeout!");
        }

        this.channel = future.channel();
        if (!future.isSuccess()){
            this.channel = null;
            close();
        }

        while (!authenticatorResponded.get()) {
            Thread.sleep(250);
        }

        if (!authenticated){
            throw new EslAuthenticateException("Authentication failed!" );
        }
    }

    /**
     * 是否连接成功
     * @return boolean
     */
    public boolean isConnected(){
        return (this.channel != null && this.channel.isActive());
    }

    /**
     * 是否可以发送指令
     * @return boolean
     */
    public boolean canSend(){
        return isConnected() && authenticated;
    }

    /**
     * 关闭连接
     */
    public void close(){
        if(this.isConnected()){
            this.channel.closeFuture();
        }

        group.shutdownGracefully();
    }

    /**
     * 检查是否可以发送消息
     */
    private void checkSend(){
        if (!isConnected()){
            throw new EslConnectException("Don't have connection with FreeSWITCH!");
        }

        if (!authenticated) {
            throw new EslAuthenticateException("Authentication failed!" );
        }
    }


    /**
     * 发送同步命令
     * @param command  command
     * @param args     arguments
     * @return EslMessage result
     */
    public EslMessage sendSyncCommand(String command, String ... args){
        checkSend();
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isEmpty(command)) {
            throw new IllegalStateException( "missing command!" );
        }
        sb.append("api");
        sb.append(" ");
        sb.append(command);
        if (args != null && args.length > 0){
            for (String arg: args){
                sb.append(" ");
                sb.append(arg);
                sb.append(" ");
            }
        }
        EslClientHandler handler = (EslClientHandler) this.channel.pipeline().last();
        return handler.sendSyncSingleLineCommand(sb.toString());
    }

    /**
     * 发送异步命令
     * @param command  command
     * @param args     arguments
     * @return String  job uuid
     */
    public String sendAsyncCommand(String command, String ... args){
        checkSend();
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isEmpty(command)) {
            throw new IllegalStateException( "missing command!" );
        }
        sb.append("bgapi");
        sb.append(" ");
        sb.append(command);
        if (args != null && args.length > 0){
            for (String arg: args){
                sb.append(" ");
                sb.append(arg);
                sb.append(" ");
            }
        }
        EslClientHandler handler = (EslClientHandler) this.channel.pipeline().last();
        return handler.sendAsyncCommand(sb.toString());
    }

    /**
     * 订阅事件
     * @param contentType  xml/json/plain
     * @param event        event name
     * @return             success
     */
    public boolean setEventSubscriptions(EslContentType contentType, String event){
        checkSend();
        EslClientHandler handler = (EslClientHandler) this.channel.pipeline().last();
        switch (contentType){
            case TEXT_EVENT_PLAIN:
                return handler.sendSyncSingleLineCommand("event plain " + event).isOk();
            case TEXT_EVENT_XML:
                return handler.sendSyncSingleLineCommand("event xml " + event).isOk();
            case TEXT_EVENT_JSON:
                return handler.sendSyncSingleLineCommand( "event json " + event).isOk();
            default:
                throw new IllegalStateException( "Unknow event format:" + contentType.type());
        }
    }

    /**
     * 取消订阅单个事件
     * @param event  event name
     * @return       success
     */
    public boolean cancelEventSubscriptions(String event){
        checkSend();
        EslClientHandler handler = (EslClientHandler) this.channel.pipeline().last();
        return handler.sendSyncSingleLineCommand("nixevent " + event).isOk();
    }

    /**
     * 取消订阅全部事件
     * @return  success
     */
    public boolean cancelEventsSubscriptions(){
        checkSend();
        EslClientHandler handler = (EslClientHandler) this.channel.pipeline().last();
        return handler.sendSyncSingleLineCommand( "noevents").isOk();
    }

    /**
     * 添加事件过滤器
     * @param headerName    event header field name
     * @param valueToHeader header value
     * @return   success
     */
    public boolean addEventFilter(String headerName, String valueToHeader){
        checkSend();
        EslClientHandler handler = (EslClientHandler) this.channel.pipeline().last();
        return handler.sendSyncSingleLineCommand(String.format("filter %s %s", headerName, valueToHeader)).isOk();
    }

    /**
     * 删除事件过滤器
     * @param headerName  event header field name
     * @param valueToHeader header value
     * @return  success
     */
    public boolean deleteEventFilter(String headerName, String valueToHeader){
        checkSend();
        EslClientHandler handler = (EslClientHandler) this.channel.pipeline().last();
        return handler.sendSyncSingleLineCommand( String.format("filter delete %s %s", headerName, valueToHeader)).isOk();
    }

    /**
     * 发送消息
     * @param eslCommand  message to send
     * @return  result
     */
    public EslMessage sendMessage(EslCommand eslCommand){
        checkSend();
        EslClientHandler handler = (EslClientHandler) this.channel.pipeline().last();
        return handler.sendSyncMultiLineCommand(eslCommand.getMsgLines());
    }

    /**
     * 设置日志等级
     * @param level level
     * @return  success
     */
    public boolean setLoggingLevel(LoggingLevel level){
        checkSend();
        EslClientHandler handler = (EslClientHandler) this.channel.pipeline().last();
        return handler.sendSyncSingleLineCommand(String.format("log %s",
                StringUtils.lowerCase(level.name()))).isOk();
    }

    /**
     * 取消日志
     * @return success
     */
    public boolean cancelLogging(){
        checkSend();
        EslClientHandler handler = (EslClientHandler) this.channel.pipeline().last();
        return handler.sendSyncSingleLineCommand("nolog").isOk();
    }

    /**
     * 认证成功回调
     * @param successful   success
     * @param failureText  description
     */
    @Override
    public void onAuthedResult(boolean successful, String failureText) {
        logger.debug("auth result ---- : {} {}", successful, failureText);
        authenticatorResponded.set(true);
        this.authenticated = successful;
        if (successful) {
            onAuthenticated();
        } else {
            throw new EslAuthenticateException("Authentication failed!" );
        }
    }

    public abstract void onAuthenticated();

    @Override
    public abstract void onEslEvent(EslEvent eslEvent);

    @Override
    public abstract void onAsyncResult(String jobUuid, List<String> result);

    @Override
    public abstract void onDisconnected();
}
