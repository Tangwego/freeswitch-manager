package top.wdcc.freeswitch.eslclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.wdcc.freeswitch.common.EslContentType;
import top.wdcc.freeswitch.common.EslEvent;
import top.wdcc.freeswitch.common.LoggingLevel;

import java.util.List;

public class EslClient extends AbstractEslClient {
    private static final Logger logger = LoggerFactory.getLogger(EslClient.class);

    @Override
    public boolean setEventSubscriptions(EslContentType contentType, String event) {
        throw new IllegalStateException("EslClient cannot set event subscriptions!");
    }

    @Override
    public boolean cancelEventsSubscriptions() {
        throw new IllegalStateException("EslClient cannot cancel event subscriptions!");
    }

    @Override
    public boolean cancelEventSubscriptions(String event) {
        throw new IllegalStateException("EslClient cannot cancel event subscriptions!");
    }

    @Override
    public boolean setLoggingLevel(LoggingLevel level) {
        throw new IllegalStateException("EslClient cannot set logging level!");
    }

    @Override
    public boolean cancelLogging() {
        throw new IllegalStateException("EslClient cannot cancel logging!");
    }

    @Override
    public boolean addEventFilter(String headerName, String valueToHeader) {
        throw new IllegalStateException("EslClient cannot add event filter!");
    }

    @Override
    public boolean deleteEventFilter(String headerName, String valueToHeader) {
        throw new IllegalStateException("EslClient cannot delete event filter!");
    }

    @Override
    public void onAuthenticated() {
    }

    @Override
    public void onEslEvent(EslEvent eslEvent) {
        logger.debug("received esl event: {}", eslEvent);
    }

    @Override
    public void onAsyncResult(String jobUuid, List<String> result) {
        logger.debug("received job result: [{}]", jobUuid);
    }

    @Override
    public void onDisconnected() {
        logger.debug("disconnected!");
    }
}
