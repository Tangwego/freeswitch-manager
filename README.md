# Custom FreeSWITCH Event Socket Library
## 自定义FreeSWITCH ESL 库

## 架构说明
- 定义一个通用抽象客户端, 用户可以进行自定义实现客户端
- 实现一个EventClient专门进行事件监听
- 实现一个EslClient专门处理命令发送
- EslEvent更加方便的获取一些常用字段和取自定义字段
- 解码器自动释放缓存
- 简化外联服务端监听器
- 将CommandResponse集成到EslMessage
- 增加XML/JSON等事件的解析
- 将事件头字段定义改成枚举
- 命令客户端EslClient发送命令等方法增强

## 安装教程
1. pom.xml中添加依赖
```
<dependency>
    <groupId>top.wdcc</groupId>
    <artifactId>freeswitch-esl</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 使用说明
- EslClient用法
```
EslClient eslClient = new EslClient();
eslClient.connect(host, port, "ClueCon", 30);
EslMessage message = eslClient.sendSyncCommand("sofia", "status");
System.out.println(message.getReplyText());
Thread.sleep(5000);
eslClient.close();
```

- EventClient用法
```
EventClient eventClient = new EventClient(new EslListener() {
    @Override
    public void onAuthedResult(boolean successful, String failureText) {
        // TODO on authencated
    }

    @Override
    public void onEslEvent(EslEvent eslEvent) {
        // TODO on esl event received
    }

    @Override
    public void onAsyncResult(String jobUuid, List<String> result) {
        // TODO on background job result received
    }

    @Override
    public void onDisconnected() {
        // TODO on client disconnected
    }
});
eventClient.connect("192.168.0.100", 8021, "ClueCon", 30);
Thread.sleep(5000);
eventClient.close();
```
- OutboundServer用法
```
OutboundServer ob = new OutboundServer(new OutboundListener() {
    @Override
    public void onOuboundEslEvent(EslEvent eslEvent) {
        // TODO on outbound event received
    }

    @Override
    public void handleDisconnected() {
        // TODO on client disconnected
    }
});
// custom port
ob.start(8080);
ob.stop();
```

## 参与贡献
    1. Fork 本仓库
    2. 新建 dev 分支
    3. 提交代码
    4. 新建 Pull Request