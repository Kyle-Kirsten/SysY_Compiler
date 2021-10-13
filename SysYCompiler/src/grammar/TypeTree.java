package grammar;

import wordTokenizer.Word;

import java.util.List;

public class TypeTree extends Node {
    private Word type;
    private List<VarDecl> vars;
    private boolean isConst;


    public TypeTree(Word type, List<VarDecl> vars, boolean isConst) {
        this.type = type;
        this.vars = vars;
        this.isConst = isConst;
    }
}
