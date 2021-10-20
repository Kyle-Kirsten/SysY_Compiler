package exceptions;

public class RBracketException extends UserException {

    public RBracketException(String message, int lineNo) {
        super(message, lineNo);
        errCode = "k";
    }
}
