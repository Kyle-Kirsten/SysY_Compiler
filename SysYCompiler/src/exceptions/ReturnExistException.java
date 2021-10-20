package exceptions;

public class ReturnExistException extends UserException {
    public ReturnExistException(String message, int lineNo) {
        super(message, lineNo);
        errCode = "f";
    }
}
