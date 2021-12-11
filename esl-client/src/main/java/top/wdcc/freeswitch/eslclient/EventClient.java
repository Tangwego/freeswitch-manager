package top.wdcc.freeswitch.eslclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.wdcc.freeswitch.common.EslCommand;
import top.wdcc.freeswitch.common.EslContentType;
import top.wdcc.freeswitch.common.EslEvent;
import top.wdcc.freeswitch.common.EslMessage;

import java.util.List;

public class EventClient extends AbstractEslClient {
    private static final Logger logger = LoggerFactory.getLogger(EventClient.class);

    private EslListener listener;

    public EventClient(EslListener eslListener){
        this.listener = eslListener;
    }
    @Override
    public void onAuthenticated() {
        setEventSubscriptions(EslContentType.TEXT_EVENT_XML, "all");
    }

    @Override
    public void onAuthedResult(boolean successful, String failureText) {
        super.onAuthedResult(successful, failureText);
        if (this.listener != null){
            this.listener.onAuthedResult(successful, failureText);
        }
    }

    @Override
    public void onEslEvent(EslEvent eslEvent) {
        if (this.listener != null){
            this.listener.onEslEvent(eslEvent);
        }
    }

    @Override
    public void onAsyncResult(String jobUuid, List<String> result) {
        if (this.listener != null){
            this.listener.onAsyncResult(jobUuid, result);
        }
    }

    @Override
    public void onDisconnected() {
        if (this.listener != null){
            this.listener.onDisconnected();
        }
    }

    @Override
    public EslMessage sendMessage(EslCommand eslCommand) {
        throw new IllegalStateException("Event client cannot send message.");
    }

    @Override
    public boolean canSend() {
        return false;
    }

    @Override
    public String sendAsyncCommand(String command, String... args) {
        throw new IllegalStateException("Event client cannot send async command.");
    }

    @Override
    public EslMessage sendSyncCommand(String command, String... args) {
        throw new IllegalStateException("Event client cannot send sync command.");
    }

}

