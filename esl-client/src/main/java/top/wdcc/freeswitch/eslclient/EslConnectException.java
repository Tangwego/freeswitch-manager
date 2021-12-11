package top.wdcc.freeswitch.eslclient;

public class EslConnectException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public EslConnectException( String message )
    {
        super( message );
    }

    public EslConnectException( String message, Throwable cause )
    {
        super( message, cause );
    }

}
