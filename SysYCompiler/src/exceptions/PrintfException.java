package exceptions;

public class PrintfException extends UserException {

    public PrintfException(String message, int lineNo) {
        super(message, lineNo);
        errCode = "l";
    }
}
