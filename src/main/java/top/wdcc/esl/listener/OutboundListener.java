package top.wdcc.esl.listener;

import top.wdcc.esl.event.EslEvent;

public interface OutboundListener {
    void onOuboundEslEvent(EslEvent eslEvent);
    void handleDisconnected();
}
