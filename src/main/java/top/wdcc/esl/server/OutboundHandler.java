package top.wdcc.esl.server;

import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.wdcc.esl.client.AbstractEslHandler;
import top.wdcc.esl.event.EslEvent;
import top.wdcc.esl.listener.OutboundListener;
import top.wdcc.esl.message.EslMessage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class OutboundHandler extends AbstractEslHandler {
    public static final Logger logger = LoggerFactory.getLogger(OutboundHandler.class);
    public final OutboundListener listener;

    private final ExecutorService executorService = Executors.newCachedThreadPool(new ThreadFactory() {
        private final AtomicInteger atomicInteger = new AtomicInteger(1);
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "Outbound-Server-" + atomicInteger.getAndIncrement());
        }
    });

    public OutboundHandler(OutboundListener outboundListener){
        this.listener = outboundListener;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        executorService.submit( () -> {
            handleOutboundMessage(ctx);
        });

    }

    @Override
    protected void handleAuthRequest(ChannelHandlerContext ctx) {
        logger.debug("handle auth request!");
        throw new IllegalStateException("outbound mode is cannot use!");
    }

    @Override
    protected void handleEslMessage(ChannelHandlerContext ctx, EslMessage message) {
        throw new IllegalStateException("outbound mode is cannot use!");
    }

    protected void handleOutboundMessage(ChannelHandlerContext ctx) {
        EslMessage eslMessage = sendSyncSingleLineCommand(ctx.channel(), "connect");
        EslEvent eslEvent = new EslEvent(eslMessage, true);
        logger.debug("post esl event: {}", eslEvent.getEventName());
        if (this.listener != null) {
            this.listener.onOuboundEslEvent(eslEvent);
        }
    }

    @Override
    protected void handleDisconnectionNotice() {
        if (this.listener != null) {
            this.listener.handleDisconnected();
        }
    }


}
