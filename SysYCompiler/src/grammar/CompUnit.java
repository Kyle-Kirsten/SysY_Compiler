package grammar;

import java.util.List;

public class CompUnit extends Node {
    private List<TypeTree> decls;
    private List<FuncDecl> funcs;

    public CompUnit(List<TypeTree> decls, List<FuncDecl> funcs) {
        this.decls = decls;
        this.funcs = funcs;
    }
}
