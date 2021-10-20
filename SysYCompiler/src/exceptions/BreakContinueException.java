package exceptions;

public class BreakContinueException extends UserException {
    public BreakContinueException(String message, int lineNo) {
        super(message, lineNo);
        errCode = "m";
    }
}
