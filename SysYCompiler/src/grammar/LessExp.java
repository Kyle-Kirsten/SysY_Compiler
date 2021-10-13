package grammar;

public class LessExp extends Node {
    private Node lval;
    private Node rval;

    public LessExp(Node lval, Node rval) {
        this.lval = lval;
        this.rval = rval;
    }
}
