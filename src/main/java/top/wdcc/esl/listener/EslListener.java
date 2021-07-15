package top.wdcc.esl.listener;

import top.wdcc.esl.event.EslEvent;

import java.util.List;

public interface EslListener{
    void onAuthedResult(boolean successful, String failureText);
    void onEslEvent(EslEvent eslEvent);
    void onAsyncResult(String jobUuid, List<String> result);
    void onDisconnected();
}
