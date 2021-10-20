package exceptions;

import java.io.BufferedWriter;
import java.io.IOException;

public class IllegalFStrCharException extends UserException {

    public IllegalFStrCharException(String message, int lineNo) {
        super(message, lineNo);
        errCode = "a";
    }

}
