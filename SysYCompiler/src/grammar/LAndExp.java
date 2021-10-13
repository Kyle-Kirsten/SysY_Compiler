package grammar;

public class LAndExp extends Node {
    private Node lval;
    private Node rval;

    public LAndExp(Node lval, Node rval) {
        this.lval = lval;
        this.rval = rval;
    }
}
