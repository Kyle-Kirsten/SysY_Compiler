package grammar;

public class VarDecl extends Node {
    private LVal lVal;
    private Node val; //未定义时为unDefined
    private Symbol varSymbol; //符号表中的数据


    public VarDecl(LVal lVal, Node val) {
        this.lVal = lVal;
        this.val = val;
    }

    public VarDecl(LVal lVal, Node val, Symbol varSymbol) {
        this(lVal, val);
        this.varSymbol = varSymbol;
    }

    public LVal getlVal() {
        return lVal;
    }

    public Symbol getVarSymbol() {
        return varSymbol;
    }

    public void setVarSymbol(Symbol varSymbol) {
        this.varSymbol = varSymbol;
    }
}
