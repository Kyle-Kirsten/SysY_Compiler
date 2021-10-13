package grammar;

public class LOrExp extends Node {
    private Node lval;
    private Node rval;

    public LOrExp(Node lval, Node rval) {
        this.lval = lval;
        this.rval = rval;
    }
}
