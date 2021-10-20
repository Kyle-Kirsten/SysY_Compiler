package exceptions;

public class RParentException extends UserException {

    public RParentException(String message, int lineNo) {
        super(message, lineNo);
        errCode = "j";
    }
}
