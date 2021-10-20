package exceptions;

public class ConstChangeException extends UserException {

    public ConstChangeException(String message, int lineNo) {
        super(message, lineNo);
        errCode = "h";
    }
}
