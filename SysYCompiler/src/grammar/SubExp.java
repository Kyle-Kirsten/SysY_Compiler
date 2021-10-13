package grammar;

public class SubExp extends Node {
    private Node lval;
    private Node rval;

    public SubExp(Node lval, Node rval) {
        this.lval = lval;
        this.rval = rval;
    }
}
