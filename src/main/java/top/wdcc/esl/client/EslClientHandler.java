package top.wdcc.esl.client;

import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.wdcc.esl.event.EslEvent;
import top.wdcc.esl.listener.EslListener;
import top.wdcc.esl.message.EslMessage;

public class EslClientHandler extends AbstractEslHandler {
    private static final Logger logger = LoggerFactory.getLogger(EslClientHandler.class);
    private final EslListener listener;
    private final String passwd;


    public EslClientHandler(String passwd, EslListener eslListener){
        this.passwd = passwd;
        this.listener = eslListener;
    }

    @Override
    protected void handleAuthRequest(ChannelHandlerContext ctx) {
        EslMessage eslMessage = sendSyncSingleLineCommand("auth " + passwd);
        listener.onAuthedResult(eslMessage.isOk(), eslMessage.getReplyText());
    }

    @Override
    protected void handleEslMessage(ChannelHandlerContext ctx, EslMessage message) {
        EslEvent event = new EslEvent(message);
        if (StringUtils.equalsIgnoreCase(event.getEventName(), "BACKGROUND_JOB")){
            listener.onAsyncResult(event.getField("Job-UUID"), event.getEventBody());
        } else {
            listener.onEslEvent(event);
        }
    }

    @Override
    protected void handleDisconnectionNotice() {
        listener.onDisconnected();
    }


}
