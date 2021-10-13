package grammar;

public enum NonTerminal {
    CompUnit, Decl, FuncDef, MainFuncDef, ConstDecl, VarDecl, BType, ConstDef, ConstExp,
    ConstInitVal, VarDef, InitVal, Exp, FuncType, FuncFParams, Block, FuncFParam, BlockItem,
    Stmt, LVal, Cond, AddExp, LOrExp, PrimaryExp, Number, UnaryExp, FuncRParams, UnaryOp, MulExp,
    RelExp, EqExp, LAndExp;


    public String format() {
        return "<" + this.toString() + ">" + "\n";
    }
}
