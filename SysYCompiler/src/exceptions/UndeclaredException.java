package exceptions;

import java.io.BufferedWriter;
import java.io.IOException;

public class UndeclaredException extends UserException {

    public UndeclaredException(String message, int lineNo) {
        super(message, lineNo);
        errCode = "c";
    }

}
