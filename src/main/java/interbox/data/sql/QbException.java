package interbox.data.sql;


public class QbException extends RuntimeException {

    public QbException(String msg, Throwable th) {
        super(msg, th);
    }

    public QbException(String msg) {
        super(msg);
    }

}
