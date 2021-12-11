package top.wdcc.freeswitch.outbound;


import top.wdcc.freeswitch.common.EslEvent;

public interface OutboundListener {
    void onOuboundEslEvent(EslEvent eslEvent);
    void handleDisconnected();
}
