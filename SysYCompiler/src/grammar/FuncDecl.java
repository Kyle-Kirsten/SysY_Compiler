package grammar;

import wordTokenizer.Word;

import java.util.List;

public class FuncDecl extends Node {
    private Word type;
    private Word ident;
    private List<FuncFParam> params;
    private Block body;


    public FuncDecl(Word type, Word ident, List<FuncFParam> params, Block body) {
        this.type = type;
        this.ident = ident;
        this.params = params;
        this.body = body;
    }
}
