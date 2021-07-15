package top.wdcc.esl.event;

import org.apache.commons.lang3.StringUtils;

public enum EslHeader {
    /**
     * {@code "Content-Type"}
     */
    CONTENT_TYPE( "Content-Type" ),
    /**
     * {@code "Content-Length"}
     */
    CONTENT_LENGTH( "Content-Length" ),
    /**
     * {@code "Reply-Text"}
     */
    REPLY_TEXT( "Reply-Text" ),
    /**
     * {@code "Job-UUID"}
     */
    JOB_UUID( "Job-UUID" ),
    /**
     * {@code "Socket-Mode"}
     */
    SOCKET_MODE( "Socket-Mode" ),
    /**
     * {@code "Control"}
     */
    Control( "Control" ),
    ;

    private final String literal;

    private EslHeader( String literal )
    {
        this.literal = literal;
    }

    public String literal()
    {
        return literal;
    }

    public static EslHeader fromLiteral( String literal ) {
        for (EslHeader name : values() ) {
            if (StringUtils.equalsIgnoreCase(name.literal, literal)) {
                return name;
            }
        }
        return null;
    }

}
