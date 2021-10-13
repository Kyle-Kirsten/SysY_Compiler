package grammar;

import wordTokenizer.Word;

import java.util.List;

public class FuncFParam extends Node {
    private Word type;
    private Word ident;
    private List<Node> arrayDims;


    public FuncFParam(Word type, Word ident, List<Node> arrayDims) {
        this.type = type;
        this.ident = ident;
        this.arrayDims = arrayDims;
    }
}
