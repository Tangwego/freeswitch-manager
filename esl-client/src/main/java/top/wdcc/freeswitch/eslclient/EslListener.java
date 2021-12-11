package top.wdcc.freeswitch.eslclient;


import top.wdcc.freeswitch.common.EslEvent;

import java.util.List;

public interface EslListener{
    void onAuthedResult(boolean successful, String failureText);
    void onEslEvent(EslEvent eslEvent);
    void onAsyncResult(String jobUuid, List<String> result);
    void onDisconnected();
}
