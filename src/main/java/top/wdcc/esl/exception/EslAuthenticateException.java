package top.wdcc.esl.exception;

public class EslAuthenticateException extends RuntimeException {
    public EslAuthenticateException() {
        super();
    }

    public EslAuthenticateException(String message) {
        super(message);
    }

    public EslAuthenticateException(String message, Throwable cause) {
        super(message, cause);
    }

    public EslAuthenticateException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }

    @Override
    public synchronized Throwable getCause() {
        return super.getCause();
    }

    @Override
    public void printStackTrace() {
        super.printStackTrace();
    }
}
