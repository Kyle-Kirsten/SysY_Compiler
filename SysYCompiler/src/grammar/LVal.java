package grammar;

import wordTokenizer.Word;

import java.util.List;

public class LVal extends Node {
    private Word ident;
    private List<Node> index;


    public LVal(Word ident, List<Node> index) {
        this.ident = ident;
        this.index = index;
    }
}
