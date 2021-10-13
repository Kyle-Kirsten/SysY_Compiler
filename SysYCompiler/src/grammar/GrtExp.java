package grammar;

public class GrtExp extends Node {
    private Node lval;
    private Node rval;

    public GrtExp(Node lval, Node rval) {
        this.lval = lval;
        this.rval = rval;
    }
}
