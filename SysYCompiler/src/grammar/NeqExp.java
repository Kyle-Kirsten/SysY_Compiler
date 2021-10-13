package grammar;

public class NeqExp extends Node {
    private Node lval;
    private Node rval;

    public NeqExp(Node lval, Node rval) {
        this.lval = lval;
        this.rval = rval;
    }
}
