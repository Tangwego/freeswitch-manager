package top.wdcc.freeswitch.common;

import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractEslHandler extends SimpleChannelInboundHandler<EslMessage> {
    private final Logger log = LoggerFactory.getLogger(AbstractEslHandler.class);
    public static final String MESSAGE_TERMINATOR = "\n\n";
    public static final String LINE_TERMINATOR = "\n";
    public static final int MAX_WAIT_TIME = 30;

    private final Lock syncLock = new ReentrantLock();
    private final Queue<Callback> syncCallbacks = new ConcurrentLinkedQueue<>();
    private Channel channel;

    private final ExecutorService executorService = Executors.newCachedThreadPool(new ThreadFactory() {
        private final AtomicInteger atomicInteger = new AtomicInteger(1);
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "EslMessageReceived-" + atomicInteger.getAndIncrement());
        }
    });

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.channel = ctx.channel();
    }

    /**
     * 读取到解码后的消息
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, EslMessage msg) throws Exception {
        log.debug("received esl message: type: {}, size: {}", msg.getContentType().type(), msg.getContentLength());
        executorService.submit(()-> {
            EslContentType contentType = msg.getContentType();
            switch (contentType) {
                case AUTH_REQUEST:
                    log.debug("Auth request received {}", msg);
                    handleAuthRequest(ctx);
                    break;
                case TEXT_DISCONNECT_NOTICE:
                    log.debug("Disconnect notice received {}", msg);
                    handleDisconnectionNotice();
                    break;
                case COMMAND_REPLY:
                case API_RESPONSE:
                    syncCallbacks.poll().handle(msg);
                    break;
                case TEXT_EVENT_JSON:
                case TEXT_EVENT_XML:
                case TEXT_EVENT_PLAIN:
                    log.debug("Received esl message: {}", msg);
                    handleEslMessage(ctx, msg);
                    break;
                default:
                    log.warn("Unexpected message content type [{}]", contentType);
                    break;
            }
        });
    }

    public EslMessage sendSyncSingleLineCommand(final String command){
        Callback callback = new Callback();
        syncLock.lock();
        try {
            syncCallbacks.add(callback);
            getChannel().writeAndFlush(command + MESSAGE_TERMINATOR);
        }finally {
            syncLock.unlock();
        }
        return callback.getResponse();
    }

    public EslMessage sendSyncSingleLineCommand(Channel channel, final String command){
        Callback callback = new Callback();
        syncLock.lock();
        try {
            syncCallbacks.add(callback);
            channel.writeAndFlush(command + MESSAGE_TERMINATOR);
        }finally {
            syncLock.unlock();
        }
        return callback.getResponse();
    }

    public EslMessage sendSyncMultiLineCommand(final List<String> commandLines )
    {
        StringBuilder sb = new StringBuilder();
        for ( String line : commandLines )
        {
            sb.append( line );
            sb.append( LINE_TERMINATOR );
        }
        sb.append( LINE_TERMINATOR );
        Callback callback = new Callback();
        syncLock.lock();
        try {

            syncCallbacks.add(callback);
            getChannel().writeAndFlush(sb.toString());
        }finally {
            syncLock.unlock();
        }
        return callback.getResponse();
    }

    public EslMessage sendSyncMultiLineCommand(Channel channel, final List<String> commandLines )
    {
        StringBuilder sb = new StringBuilder();
        for ( String line : commandLines )
        {
            sb.append( line );
            sb.append( LINE_TERMINATOR );
        }
        sb.append( LINE_TERMINATOR );
        Callback callback = new Callback();
        syncLock.lock();
        try {

            syncCallbacks.add(callback);
            channel.writeAndFlush(sb.toString());
        }finally {
            syncLock.unlock();
        }
        return callback.getResponse();
    }

    public String sendAsyncCommand( final String command )
    {
        EslMessage response = sendSyncSingleLineCommand(command );
        if ( response.hasHeader(EslHeader.JOB_UUID) )
        {
            return response.getHeaderValue(EslHeader.JOB_UUID);
        }
        else
        {
            throw new IllegalStateException( "Missing Job-UUID header in bgapi response" );
        }
    }

    public String sendAsyncCommand(Channel channel, final String command )
    {
        EslMessage response = sendSyncSingleLineCommand(channel, command );
        if ( response.hasHeader(EslHeader.JOB_UUID) )
        {
            return response.getHeaderValue(EslHeader.JOB_UUID);
        }
        else
        {
            throw new IllegalStateException( "Missing Job-UUID header in bgapi response" );
        }
    }

    public Channel getChannel(){
        return this.channel;
    }

    protected abstract void handleAuthRequest( ChannelHandlerContext ctx );

    protected abstract void handleEslMessage(ChannelHandlerContext ctx, EslMessage message);

    protected abstract void handleDisconnectionNotice();

    private final class Callback{
        private EslMessage response;
        private final CountDownLatch promise = new CountDownLatch(1);

        public void handle(EslMessage message){
            this.response = message;
            this.promise.countDown();
            log.debug("handle esl message: {}", message);
        }

        public EslMessage getResponse(){
            try {
                log.debug("promise waiting...");
                this.promise.await(MAX_WAIT_TIME, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.debug("promise done.");
            return this.response;
        }
    }
}
