package exceptions;

import java.io.BufferedWriter;
import java.io.IOException;

public class ParamNumException extends UserException {

    public ParamNumException(String message, int lineNo) {
        super(message, lineNo);
        errCode = "d";
    }

}
