package top.wdcc.esl.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.handler.codec.TooLongFrameException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.wdcc.esl.event.EslHeader;
import top.wdcc.esl.message.EslMessage;
import top.wdcc.esl.utils.EslEventUtils;

import java.util.List;

public class EslEventDecoder extends ReplayingDecoder<EslEventDecoder.State> {
    private static final Logger logger = LoggerFactory.getLogger(EslEventDecoder.class);
    /**
     * Line feed character
     */
    static final byte LF = 10;

    protected enum State {
        READ_HEADER,
        READ_BODY,
    }

    private static final int MAX_HEADER_SIZE = 8192;
    private EslMessage currentMessage;
    private boolean treatUnknownHeadersAsBody = false;

    public EslEventDecoder(){
        this(false);
    }

    public EslEventDecoder(boolean treatUnknownHeadersAsBody){
        super(State.READ_HEADER);
        this.treatUnknownHeadersAsBody = treatUnknownHeadersAsBody;
    }

    protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {
        State state = state();
        logger.debug("decode() : state [{}]", state);
        switch (state) {
            case READ_HEADER:
                if (currentMessage == null) {
                    currentMessage = new EslMessage();
                }
                boolean reachedDoubleLF = false;
                while (!reachedDoubleLF) {
                    String headerLine = readToLineFeedOrFail(buffer, MAX_HEADER_SIZE);
                    logger.debug("read header line [{}]", headerLine);
                    if (StringUtils.isNotEmpty(headerLine)) {
                        String[] headerParts = EslEventUtils.splitHeader(headerLine);
                        EslHeader headerName = EslHeader.fromLiteral(headerParts[0]);
                        if (headerName == null) {
                            if (treatUnknownHeadersAsBody) {
                                currentMessage.addBodyLine(headerLine);
                            } else {
                                throw new IllegalStateException("Unhandled ESL header [" + headerParts[0] + ']');
                            }
                        }
                        currentMessage.addHeader(headerName, headerParts[1]);
                    } else {
                        reachedDoubleLF = true;
                    }
                    // do not read in this line again
                    checkpoint();
                }
                // have read all headers - check for content-length
                if (currentMessage.hasContentLength()) {
                    checkpoint(State.READ_BODY);
                    logger.debug("have content-length, decoding body ..");
                    //  force the next section
                    return;
                } else {
                    // end of message
                    checkpoint(State.READ_HEADER);
                    // send message upstream
                    EslMessage decodedMessage = currentMessage;
                    currentMessage = null;
                    out.add(decodedMessage);
                    return;
                }

            case READ_BODY:
                /*
                 *   read the content-length specified
                 */
                int contentLength = currentMessage.getContentLength();
                ByteBuf bodyBytes = buffer.readBytes(contentLength);
                logger.debug("read [{}] body bytes", bodyBytes.writerIndex());
                // most bodies are line based, so split on LF
                while (bodyBytes.isReadable()) {
                    String bodyLine = readLine(bodyBytes, contentLength);
                    logger.debug("read body line [{}]", bodyLine);
                    currentMessage.addBodyLine(bodyLine);
                }

                // end of message
                checkpoint(State.READ_HEADER);
                // send message upstream
                EslMessage decodedMessage = currentMessage;
                currentMessage = null;
                out.add(decodedMessage);
                return;

            default:
                throw new Error("Illegal state: [" + state + ']');
        }
    }


    private String readToLineFeedOrFail(ByteBuf buffer, int maxLineLegth) throws TooLongFrameException {
        StringBuilder sb = new StringBuilder();
        while (true) {
            // this read might fail
            byte nextByte = buffer.readByte();
            if (nextByte == LF) {
                return sb.toString();
            } else {
                // Abort decoding if the decoded line is too large.
                if (sb.length() >= maxLineLegth) {
                    throw new TooLongFrameException(
                            "ESL header line is longer than " + maxLineLegth + " bytes.");
                }
                sb.append((char) nextByte);
            }
        }
    }

    private String readLine(ByteBuf buffer, int maxLineLength) throws TooLongFrameException {
        StringBuilder sb = new StringBuilder();
        while (buffer.isReadable()) {
            // this read should always succeed
            byte nextByte = buffer.readByte();
            if (nextByte == LF) {
                return sb.toString();
            } else {
                // Abort decoding if the decoded line is too large.
                if (sb.length() >= maxLineLength) {
                    throw new TooLongFrameException(
                            "ESL message line is longer than " + maxLineLength + " bytes.");
                }
                sb.append((char) nextByte);
            }
        }

        return sb.toString();
    }
}

