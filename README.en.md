# Custom FreeSWITCH Event Socket Library
## Description
Custom Event Socket Library for FreeSWITCH

## Software Architecture
- Define an abstract client to be helpful that implement a client for youself.
- Class EventClient for watching events.
- Class EslClient for send command.
- Class EslEvent is so simple to handle the event field.
- Esl decoder is auto release now.
- Simplefiy outbound server.
- Deprecated CommandResponse and EslMessage insteaded of.
- Add XML/JSON event parser.
- Change event header to express as enum.
- Enhance class EslClient for inbound mode.

## Installation
1. Add dependency to pom.xml
```
<dependency>
    <groupId>top.wdcc</groupId>
    <artifactId>freeswitch-esl</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Usage
- Usage for class EslClient
```
EslClient eslClient = new EslClient();
eslClient.connect(host, port, "ClueCon", 30);
EslMessage message = eslClient.sendSyncCommand("sofia", "status");
System.out.println(message.getReplyText());
Thread.sleep(5000);
eslClient.close();
```

- Usage for class EventClient
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
- Usage for class OutboundServer
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
    1. Fork the repository
    2. Create dev branch
    3. Commit your code
    4. Create Pull Request