package grammar;

public class MulExp extends Node {
    private Node lval;
    private Node rval;

    public MulExp(Node lval, Node rval) {
        this.lval = lval;
        this.rval = rval;
    }
}
