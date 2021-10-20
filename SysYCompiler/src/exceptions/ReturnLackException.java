package exceptions;

public class ReturnLackException extends UserException {

    public ReturnLackException(String message, int lineNo) {
        super(message, lineNo);
        errCode = "g";
    }
}
