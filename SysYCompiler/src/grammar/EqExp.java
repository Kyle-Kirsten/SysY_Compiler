package grammar;

public class EqExp extends Node {
    private Node lval;
    private Node rval;

    public EqExp(Node lval, Node rval) {
        this.lval = lval;
        this.rval = rval;
    }
}
