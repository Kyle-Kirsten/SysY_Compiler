package grammar;

import wordTokenizer.Word;

import java.util.List;

public class FuncCall extends Node {
    private Word ident;
    private List<Node> params;

    public FuncCall(Word ident, List<Node> params) {
        this.ident = ident;
        this.params = params;
    }
}
