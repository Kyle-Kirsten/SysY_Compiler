package grammar;

import wordTokenizer.Word;

import java.util.List;

public class PrintExp extends Node {
    private Word fStr;
    private List<Node> params;


    public PrintExp(Word fStr, List<Node> params) {
        this.fStr = fStr;
        this.params = params;
    }
}
