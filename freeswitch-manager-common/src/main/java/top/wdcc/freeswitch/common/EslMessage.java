package top.wdcc.freeswitch.common;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EslMessage {
    /**
     * {@code "+OK"}
     */
    public static final String OK = "+OK";
    /**
     * {@code "-ERR invalid"}
     */
    public static final String ERR_INVALID = "-ERR invalid";
    /**
     * {@code "-ERR"}
     */
    public static final String ERR = "-ERR";

    private Map<EslHeader, String> headers;
    private List<String> bodyLines;
    private int contentLength = 0;

    public EslMessage(){
        headers = new HashMap<>();
        bodyLines = new ArrayList<>();
    }

    public Map<EslHeader, String> getHeaders() {
        return headers;
    }

    public String getHeaderValue(EslHeader header) {
        return this.headers.get(header);
    }

    public boolean hasHeader(EslHeader header){
        return this.headers.containsKey(header);
    }

    public boolean hasContentLength(){
        return this.headers.containsKey(EslHeader.CONTENT_LENGTH);
    }

    public int getContentLength(){
        if (contentLength > 0){
            return contentLength;
        }
        if (hasContentLength()) {
            try {
                contentLength = Integer.parseInt(headers.get(EslHeader.CONTENT_LENGTH));
            }catch (Exception e){
                throw new RuntimeException("parse content length failed!");
            }
        }
        return contentLength;
    }

    public EslContentType getContentType(){
        return EslContentType.fromLiteral(this.headers.get(EslHeader.CONTENT_TYPE));
    }

    public List<String> getBodyLines() {
        return bodyLines;
    }

    public void addHeader(EslHeader header, String value) {
        this.headers.put(header, value);
    }

    public String getReplyText(){
        if (EslContentType.COMMAND_REPLY.equals(EslContentType.fromLiteral(this.headers.get(EslHeader.CONTENT_TYPE)))) {
            return this.headers.get(EslHeader.REPLY_TEXT);
        }
        StringBuilder sb = new StringBuilder();
        if (ObjectUtils.isNotEmpty(this.bodyLines)) {
            for (String str: this.bodyLines) {
                sb.append(str);
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    public boolean isOk(){
        if (EslContentType.COMMAND_REPLY.equals(EslContentType.fromLiteral(this.headers.get(EslHeader.CONTENT_TYPE)))) {
            return StringUtils.startsWith(this.headers.get(EslHeader.REPLY_TEXT), OK);
        }
        return (!StringUtils.startsWith(this.headers.get(EslHeader.REPLY_TEXT), ERR));
    }

    public void addBodyLine(String line){
        if (line == null){
            return;
        }
        this.bodyLines.add(line);
    }

    @Override
    public String toString() {
        return "EslMessage:" +
                "header: [" + headers.size() + "]," +
                "body: [" + bodyLines.size() + "]";
    }
}

