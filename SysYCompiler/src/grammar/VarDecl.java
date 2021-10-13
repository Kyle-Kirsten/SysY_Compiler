package grammar;

public class VarDecl extends Node {
    private LVal lVal;
    private Node val; //未定义时为unDefined


    public VarDecl(LVal lVal, Node val) {
        this.lVal = lVal;
        this.val = val;
    }
}
