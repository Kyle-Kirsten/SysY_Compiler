package exceptions;

import java.io.BufferedWriter;
import java.io.IOException;

public class ParamTypeException extends UserException {

    public ParamTypeException(String message, int lineNo) {
        super(message, lineNo);
        errCode = "e";
    }

}
