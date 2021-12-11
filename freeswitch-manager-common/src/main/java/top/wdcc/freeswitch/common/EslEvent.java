package top.wdcc.freeswitch.common;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import top.wdcc.freeswitch.common.utils.EslEventUtils;
import top.wdcc.freeswitch.common.utils.JsonUtils;
import top.wdcc.freeswitch.common.utils.XmlUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EslEvent {
    private static final String EVENT_NAME = "Event-Name";
    private static final String EVENT_SUBCLASS = "Event-Subclass";
    private static final String CORE_UUID = "Core-UUID";
    private static final String HOST_NAME = "FreeSWITCH-Hostname";
    private static final String SWITCH_NAME = "FreeSWITCH-Switchname";
    private static final String IPV4 = "FreeSWITCH-IPv4";
    private static final String IPV6 = "FreeSWITCH-IPv6";
    private static final String EVENT_DATE_LOCAL = "Event-Date-Local";
    private static final String EVENT_DATE_GMT = "Event-Date-GMT";
    private static final String EVENT_DATE_TIMESTAMP = "Event-Date-Timestamp";
    private static final String EVENT_SEQUENCE = "Event-Sequence";
    private Map<String, String> eventMap;
    private List<String> eventBody;

    public EslEvent(EslMessage message){
        this(message,false);
    }

    public EslEvent(EslMessage message, boolean isOutbound){
        eventMap = new HashMap<>();
        eventBody = new ArrayList<>();
        switch (message.getContentType()){
            case TEXT_EVENT_PLAIN:
                parsePlainEvent(message.getBodyLines());
                break;
            case TEXT_EVENT_JSON:
                parseJsonEvent(message.getBodyLines());
                break;
            case TEXT_EVENT_XML:
                parseXmlEvent(message.getBodyLines());
                break;
            case COMMAND_REPLY:
                if (isOutbound) {
                    parsePlainEvent(message.getBodyLines());
                }
                break;
            default:
                throw new IllegalStateException("Unexpected EVENT content-type: " +
                        message.getContentType());
        }
    }

    /**
     * 解析json事件
     * @param rawBodyLines
     */
    private void parseJsonEvent(List<String> rawBodyLines) {
        for ( String rawLine : rawBodyLines ) {
            Map<String, String> map = JsonUtils.string2Map(rawLine);
            this.eventMap.putAll(map);
            String body = map.get("_body");
            if (StringUtils.isNotEmpty(body)) {
                String[] bodyLines = StringUtils.split(body, "\n");
                if (!StringUtils.isAnyEmpty(bodyLines)) {
                    for (String line : bodyLines){
                        this.eventBody.add(line);
                    }
                }
            }
        }
    }

    /**
     * 解析xml事件
     * @param rawBodyLines
     */
    private void parseXmlEvent(List<String> rawBodyLines) {
        StringBuilder sb = new StringBuilder();
        for ( String rawLine : rawBodyLines ) {
            sb.append(rawLine + "\n");
        }
        Document document = XmlUtils.parse(sb.toString());
        eventMap.putAll(XmlUtils.xml2Map(document, "/event/headers"));
        String body = XmlUtils.getValueByXmlPath(document.getRootElement(), "/event/body");
        if (StringUtils.isNotEmpty(body)) {
            String[] bodyLines = StringUtils.split(body, "\n");
            if (!StringUtils.isAnyEmpty(bodyLines)) {
                for (String line : bodyLines){
                    this.eventBody.add(line);
                }
            }
        }

    }

    /**
     * 解析plain事件
     * @param rawBodyLines
     */
    private void parsePlainEvent(List<String> rawBodyLines){
        boolean isEventBody = false;
        for ( String rawLine : rawBodyLines ) {
            if ( ! isEventBody ) {
                String[] headerParts = EslEventUtils.splitHeader(rawLine);
                try
                {
                    String decodedValue = URLDecoder.decode( headerParts[1], "UTF-8" );
                    eventMap.put(headerParts[0], decodedValue );
                }
                catch ( UnsupportedEncodingException e )
                {
                    eventMap.put( headerParts[0], headerParts[1] );
                }

                if (EslHeader.CONTENT_LENGTH.equals(EslHeader.fromLiteral(headerParts[0]) ) )
                {
                    isEventBody = true;
                }
            } else {
                if (StringUtils.isNotEmpty(rawLine)) {
                    eventBody.add(rawLine);
                }
            }
        }
    }

    public String getEventName() {
        return eventMap.get(EVENT_NAME);
    }

    public String getSubClassName() {
        return eventMap.get(EVENT_SUBCLASS);
    }

    public String getCoreUuid() {
        return eventMap.get(CORE_UUID);
    }

    public String getHostName() {
        return eventMap.get(HOST_NAME);
    }

    public String getSwitchName() {
        return eventMap.get(SWITCH_NAME);
    }

    public String getIpv4() {
        return eventMap.get(IPV4);
    }

    public String getIpv6() {
        return eventMap.get(IPV6);
    }

    public String getEventDateLocal() {
        return eventMap.get(EVENT_DATE_LOCAL);
    }

    public String getEventDateGMT() {
        return eventMap.get(EVENT_DATE_GMT);
    }

    public String getEventDateTimestamp() {
        return eventMap.get(EVENT_DATE_TIMESTAMP);
    }

    public String getEventSequence() {
        return eventMap.get(EVENT_SEQUENCE);
    }

    public Map<String, String> getEventMap() {
        return eventMap;
    }

    public String getField(String key){
        if (this.eventMap != null) {
            return this.eventMap.get(key);
        }
        return null;
    }

    public List<String> getEventBody() {
        return eventBody;
    }

    @Override
    public String toString() {
        return "EslEvent:" +
                "eventMap: [" + eventMap.size() + "]" +
                ", eventBody: [" + eventBody.size() + "]";
    }
}
