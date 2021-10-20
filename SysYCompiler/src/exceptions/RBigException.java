package exceptions;

public class RBigException extends UserException {

    public RBigException(String message, int lineNo) {
        super(message, lineNo);
        errCode = "n";
    }
}
