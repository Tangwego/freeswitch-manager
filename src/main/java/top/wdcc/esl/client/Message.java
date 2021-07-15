package top.wdcc.esl.client;

import java.util.ArrayList;
import java.util.List;

public class Message {
    public static final String COMMAND = "sendmsg";
    private final List<String> msgLines = new ArrayList<String>();
    private final boolean hasUuid;

    /**
     * Constructor for use with outbound socket client only.  This client mode does not need a call
     * UUID for context.
     */
    public Message()
    {
        msgLines.add(COMMAND);
        hasUuid = false;
    }

    /**
     * Constructor for use with the inbound client.
     *
     * @param uuid of the call to send message to (it should be in 'park' to be operated on).
     */
    public Message( String uuid )
    {
        msgLines.add( COMMAND + " " + uuid );
        hasUuid = true;
    }

    /**
     * Adds the following line to the message:
     * <pre>
     *   call-command: command
     * </pre>
     * @param command the string command [ execute | hangup ]
     */
    public void addCallCommand( String command )
    {
        msgLines.add( "call-command: " + command );
    }

    /**
     * Adds the following line to the message:
     * <pre>
     *   execute-app-name: appName
     * </pre>
     * @param appName the string app name to execute
     */
    public void addExecuteAppName( String appName )
    {
        msgLines.add( "execute-app-name: " + appName );
    }

    /**
     * Adds the following line to the message:
     * <pre>
     *   execute-app-arg: arg
     * </pre>
     * @param arg the string arg
     */
    public void addExecuteAppArg( String arg )
    {
        msgLines.add( "execute-app-arg: " + arg );
    }

    /**
     * Adds the following line to the message:
     * <pre>
     *   loops: count
     * </pre>
     * @param count the int number of times to loop
     */
    public void addLoops( int count )
    {
        msgLines.add( "loops: " + count );
    }

    /**
     * Adds the following line to the message:
     * <pre>
     *   hangup-cause: cause
     * </pre>
     * @param cause the string cause
     */
    public void addHangupCause( String cause )
    {
        msgLines.add( "hangup-cause: " + cause );
    }

    /**
     * Adds the following line to the message:
     * <pre>
     *   nomedia-uid: value
     * </pre>
     * @param value the string value part of the line
     */
    public void addNomediaUuid( String value )
    {
        msgLines.add( "nomedia-uuid: " + value );
    }

    /**
     *  Adds the following line to the message:
     *  <pre>
     *    event-lock: true
     *  </pre>
     */
    public void addEventLock()
    {
        msgLines.add( "event-lock: true" );
    }

    /**
     * A generic method to add a message line. The constructed line in the sent message will be in the
     * form:
     * <pre>
     *   name: value
     * </pre>
     *
     * @param name part of line
     * @param value part of line
     */
    public void addGenericLine( String name, String value )
    {
        msgLines.add( name + ": " + value );
    }

    /**
     * The list of strings that make up the message to send to FreeSWITCH.
     *
     * @return list of strings, as they were added to this message.
     */
    public List<String> getMsgLines()
    {
        return msgLines;
    }

    /**
     * Indicate if message was constructed with a UUID.
     *
     * @return true if constructed with a UUID.
     */
    public boolean hasUuid()
    {
        return hasUuid;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder( "SendMsg: " );
        if ( msgLines.size() > 1 )
        {
            sb.append( msgLines.get( 1 ) );
        }
        else if ( msgLines.size() > 0 )
        {
            sb.append( 0 );
        }

        return sb.toString();
    }

}
