package exceptions;

import java.io.BufferedWriter;
import java.io.IOException;

public abstract class UserException extends Exception {
    protected int lineNo;
    protected String errCode;

    public UserException(String message, int lineNo) {
        super(message);
        this.lineNo = lineNo;
    }

    public void prtError(BufferedWriter output) {
        try {
            output.write(lineNo + " " + errCode + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
