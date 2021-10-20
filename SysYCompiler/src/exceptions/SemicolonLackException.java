package exceptions;

public class SemicolonLackException extends UserException {
    public SemicolonLackException(String message, int lineNo) {
        super(message, lineNo);
        errCode = "i";
    }
}
