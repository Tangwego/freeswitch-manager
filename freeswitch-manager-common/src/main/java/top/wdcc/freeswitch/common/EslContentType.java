package top.wdcc.freeswitch.common;

import org.apache.commons.lang3.StringUtils;

public enum EslContentType {
    /**
     * {@code "auth/request"}
     */
    AUTH_REQUEST("auth/request"),
    /**
     * {@code "api/response"}
     */
    API_RESPONSE("api/response"),
    /**
     * {@code "command/reply"}
     */
    COMMAND_REPLY("command/reply"),
    /**
     * {@code "text/event-plain"}
     */
    TEXT_EVENT_PLAIN("text/event-plain"),
    /**
     * {@code "text/event-xml"}
     */
    TEXT_EVENT_XML("text/event-xml"),
    /**
     * {@code "text/event-json"}
     */
    TEXT_EVENT_JSON("text/event-json"),
    /**
     * {@code "text/disconnect-notice"}
     */
    TEXT_DISCONNECT_NOTICE("text/disconnect-notice"),
    ;
    private String type;
    private EslContentType(String type){
        this.type = type;
    }

    public String type()
    {
        return type;
    }

    public static EslContentType fromLiteral( String type ) {
        for (EslContentType contentType : values() ) {
            if (StringUtils.equalsIgnoreCase(contentType.type, type)) {
                return contentType;
            }
        }
        return null;
    }
}