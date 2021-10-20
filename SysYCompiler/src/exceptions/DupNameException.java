package exceptions;

import java.io.BufferedWriter;
import java.io.IOException;

public class DupNameException extends UserException {

    public DupNameException(String message, int lineNo) {
        super(message, lineNo);
        errCode = "b";
    }

}
